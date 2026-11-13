package gov.cmr.minfi.db.gbe.app.depense.soumission.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.common.pagination.Tris;
import gov.cmr.minfi.db.gbe.app.common.reference.ReferenceGenerator;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoinRepository;
import gov.cmr.minfi.db.gbe.app.depense.soumission.OffreLigne;
import gov.cmr.minfi.db.gbe.app.depense.soumission.Soumission;
import gov.cmr.minfi.db.gbe.app.depense.soumission.SoumissionMapper;
import gov.cmr.minfi.db.gbe.app.depense.soumission.SoumissionRepository;
import gov.cmr.minfi.db.gbe.app.depense.soumission.SoumissionService;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.SoumissionRequest;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.SoumissionResponse;
import gov.cmr.minfi.db.gbe.app.depense.taxe.CalculTaxe;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.CoordonneeBancaire;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.Fournisseur;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.FournisseurConnecte;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoumissionServiceImpl implements SoumissionService {

    static final String PREFIXE_REFERENCE = "SO";
    static final Set<String> TRIS_AUTORISES = Set.of("dateSoumission", "numero");

    private final SoumissionRepository soumissionRepository;
    private final ExpressionBesoinRepository besoinRepository;
    private final FournisseurConnecte fournisseurConnecte;
    private final CalculTaxe calculTaxe;
    private final ReferenceGenerator referenceGenerator;
    private final SoumissionMapper soumissionMapper;
    private final Clock clock;

    @Override
    @Transactional
    public SoumissionResponse deposer(SoumissionRequest request) {
        final Fournisseur fournisseur = fournisseurConnecte.exiger();
        final LocalDateTime maintenant = LocalDateTime.now(clock);
        final ExpressionBesoin besoin = besoinRepository.findById(request.besoinId())
                .filter(b -> b.isOuverteAuxSoumissions(maintenant))
                .orElseThrow(() -> new BusinessException(ErrorCode.BESOIN_NON_OUVERT, request.besoinId()));
        if (soumissionRepository.existsByExpressionBesoinIdAndFournisseurId(besoin.getId(), fournisseur.getId())) {
            throw new BusinessException(ErrorCode.SOUMISSION_DEJA_DEPOSEE, besoin.getReference());
        }

        final Soumission soumission = Soumission.builder()
                .expressionBesoin(besoin)
                .fournisseur(fournisseur)
                .dateSoumission(maintenant)
                .build();
        appliquer(soumission, request);
        soumission.setNumero(referenceGenerator.prochaineReference(PREFIXE_REFERENCE, besoin.getExercice().getAnnee()));
        soumission.sceller(maintenant);
        soumissionRepository.save(soumission);
        log.info("Soumission {} déposée par {} sur {}", soumission.getNumero(), fournisseur.getNiu(), besoin.getReference());
        return soumissionMapper.toResponse(soumission);
    }

    @Override
    @Transactional
    public SoumissionResponse modifier(String soumissionId, SoumissionRequest request) {
        final Soumission soumission = findMaSoumission(soumissionId);
        final LocalDateTime maintenant = LocalDateTime.now(clock);
        soumission.verifierModifiable(maintenant);
        soumission.getLignes().clear();
        soumissionRepository.flush(); // supprime les anciennes lignes avant d'insérer les nouvelles (unicité)
        appliquer(soumission, request);
        soumission.setDateDerniereModification(maintenant);
        soumission.sceller(maintenant);
        log.info("Soumission {} modifiée", soumission.getNumero());
        return soumissionMapper.toResponse(soumission);
    }

    @Override
    public SoumissionResponse getMaSoumission(String soumissionId) {
        return soumissionMapper.toResponse(findMaSoumission(soumissionId));
    }

    @Override
    public PageResponse<SoumissionResponse> getMesSoumissions(Pageable pageable) {
        final Fournisseur fournisseur = fournisseurConnecte.exiger();
        final Pageable page = Tris.verifier(pageable, TRIS_AUTORISES, Sort.by(Sort.Direction.DESC, "dateSoumission"));
        return PageResponse.of(soumissionRepository.findByFournisseurId(fournisseur.getId(), page),
                soumissionMapper::toResume);
    }

    // ── Interne ───────────────────────────────────────

    private void appliquer(Soumission soumission, SoumissionRequest request) {
        final CoordonneeBancaire rib = soumission.getFournisseur().getCoordonneesBancaires().stream()
                .filter(c -> c.getId().equals(request.coordonneeBancaireId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RIB_NOT_FOUND, request.coordonneeBancaireId()));
        soumission.choisirRib(rib);
        soumission.setDelaiProposeJours(request.delaiProposeJours());
        final List<OffreLigne> offre = request.lignes().stream()
                .map(l -> new OffreLigne(l.ligneBesoinId(), l.prixUnitairePropose(), l.remarquesTechniques()))
                .toList();
        soumission.definirOffre(offre);
        soumission.appliquerTaxes(calculTaxe.calculer(soumission.getExpressionBesoin(), soumission.getMontantProposeHT()));
    }

    /** Une soumission d'un autre fournisseur répond 404, comme un identifiant inconnu. */
    private Soumission findMaSoumission(String soumissionId) {
        final Fournisseur fournisseur = fournisseurConnecte.exiger();
        return soumissionRepository.findByIdAndFournisseurId(soumissionId, fournisseur.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUMISSION_NOT_FOUND, soumissionId));
    }
}
