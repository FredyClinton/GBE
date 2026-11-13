package gov.cmr.minfi.db.gbe.app.identite.fournisseur.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.identite.auth.tfa.TwoFactorAuthenticationService;
import gov.cmr.minfi.db.gbe.app.depense.soumission.SoumissionRepository;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.CoordonneeBancaire;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.FournisseurConnecte;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.Fournisseur;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.FournisseurMapper;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.FournisseurRepository;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.FournisseurService;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.CreateFournisseurRequest;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.FournisseurResponse;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.RibRequest;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.UpdateFournisseurRequest;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.Role;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleRepository;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import gov.cmr.minfi.db.gbe.app.identite.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FournisseurServiceImpl implements FournisseurService {

    private final FournisseurRepository fournisseurRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorAuthenticationService tfaService;
    private final FournisseurMapper fournisseurMapper;
    private final FournisseurConnecte fournisseurConnecte;
    private final SoumissionRepository soumissionRepository;

    @Override
    @Transactional
    public FournisseurResponse createFournisseur(CreateFournisseurRequest request) {
        final String niu = request.niu().trim();
        if (fournisseurRepository.existsByNiu(niu)) {
            throw new BusinessException(ErrorCode.NUI_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByPhoneNumberIgnoreCase(request.telephoneRepresentant())) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }
        final String nomRole = "ROLE_" + RoleSysteme.FOURNISSEUR.name();
        final Role role = roleRepository.findByName(nomRole)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, nomRole));

        final User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .firstName(request.prenomRepresentant())
                .lastName(request.nomRepresentant())
                .phoneNumber(request.telephoneRepresentant())
                .nui(niu)
                .enabled(true)
                .locked(false)
                .credentialsExpired(false)
                .emailVerified(false)
                .phoneVerified(false)
                .firstLogin(true)
                .secret(tfaService.generateNewSecret())
                .build();
        userRepository.save(user);

        final Fournisseur fournisseur = Fournisseur.builder()
                .raisonSociale(request.raisonSociale())
                .niu(niu)
                .rccm(request.rccm())
                .adresse(request.adresse())
                .telephone(request.telephone())
                .user(user)
                .build();
        if (request.ribs() != null) {
            request.ribs().forEach(rib -> fournisseur.ajouterRib(ribValide(rib)));
        }
        fournisseurRepository.save(fournisseur);
        log.info("Fournisseur créé : {} (NIU {}), compte {}", fournisseur.getRaisonSociale(), niu, user.getEmail());
        return fournisseurMapper.toResponse(fournisseur);
    }

    @Override
    public List<FournisseurResponse> getAllFournisseurs() {
        return fournisseurRepository.findAllByOrderByRaisonSocialeAsc().stream()
                .map(fournisseurMapper::toResponse)
                .toList();
    }

    @Override
    public FournisseurResponse getFournisseur(String fournisseurId) {
        return fournisseurMapper.toResponse(findFournisseur(fournisseurId));
    }

    @Override
    @Transactional
    public FournisseurResponse updateFournisseur(String fournisseurId, UpdateFournisseurRequest request) {
        final Fournisseur fournisseur = findFournisseur(fournisseurId);
        final String niu = request.niu().trim();
        if (fournisseurRepository.existsByNiuAndIdNot(niu, fournisseurId)) {
            throw new BusinessException(ErrorCode.NUI_ALREADY_EXISTS);
        }
        fournisseur.setRaisonSociale(request.raisonSociale());
        fournisseur.setNiu(niu);
        fournisseur.setRccm(request.rccm());
        fournisseur.setAdresse(request.adresse());
        fournisseur.setTelephone(request.telephone());
        fournisseur.getUser().setNui(niu);
        log.info("Fournisseur modifié : {}", fournisseurId);
        return fournisseurMapper.toResponse(fournisseur);
    }

    @Override
    @Transactional
    public FournisseurResponse ajouterRib(String fournisseurId, RibRequest request) {
        final Fournisseur fournisseur = findFournisseur(fournisseurId);
        fournisseur.ajouterRib(ribValide(request.rib()));
        // Entité gérée : le flush persiste le nouveau RIB (cascade) et lui attribue son id
        fournisseurRepository.flush();
        return fournisseurMapper.toResponse(fournisseur);
    }

    @Override
    @Transactional
    public FournisseurResponse retirerRib(String fournisseurId, String coordonneeBancaireId) {
        final Fournisseur fournisseur = findFournisseur(fournisseurId);
        if (soumissionRepository.existsByCoordonneeBancaireId(coordonneeBancaireId)) {
            throw new BusinessException(ErrorCode.RIB_UTILISE, coordonneeBancaireId);
        }
        fournisseur.retirerRib(coordonneeBancaireId); // orphanRemoval : supprimé au commit
        return fournisseurMapper.toResponse(fournisseur);
    }

    @Override
    public FournisseurResponse getMonProfil() {
        return fournisseurMapper.toResponse(fournisseurConnecte.exiger());
    }

    private Fournisseur findFournisseur(String fournisseurId) {
        return fournisseurRepository.findById(fournisseurId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOURNISSEUR_NOT_FOUND, fournisseurId));
    }

    private static String ribValide(String rib) {
        final String normalise = CoordonneeBancaire.normaliserRib(rib);
        if (!CoordonneeBancaire.isRibValide(normalise)) {
            throw new BusinessException(ErrorCode.RIB_INVALIDE, rib);
        }
        return normalise;
    }
}
