package gov.cmr.minfi.db.gbe.app.depense.engagement.impl;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaireRepository;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.common.pagination.Tris;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoinRepository;
import gov.cmr.minfi.db.gbe.app.depense.besoin.StatutExpression;
import gov.cmr.minfi.db.gbe.app.depense.engagement.*;
import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.ActeJuridiqueResponse;
import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.CreationActeRequest;
import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.ModificationActeRequest;
import gov.cmr.minfi.db.gbe.app.depense.soumission.Soumission;
import gov.cmr.minfi.db.gbe.app.depense.soumission.SoumissionRepository;
import gov.cmr.minfi.db.gbe.app.depense.soumission.StatutSoumission;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatScopeValidator;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatScopeValidator.Perimetre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ActeJuridiqueServiceImpl implements ActeJuridiqueService {

    static final Set<String> TRIS_AUTORISES = Set.of("numero", "dateEmission", "montantTTC", "statut");
    private static final String AUCUN_ID = "-";

    private final ActeJuridiqueRepository acteRepository;
    private final ExpressionBesoinRepository besoinRepository;
    private final SoumissionRepository soumissionRepository;
    private final CreditBudgetaireRepository creditRepository;
    private final ActeJuridiqueFactory acteFactory;
    private final MandatScopeValidator mandatScopeValidator;
    private final ActeJuridiqueMapper acteMapper;
    private final Clock clock;

    @Override
    @Transactional
    public ActeJuridiqueResponse creer(CreationActeRequest request) {
        final ExpressionBesoin besoin = besoinRepository.findById(request.besoinId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BESOIN_NOT_FOUND, request.besoinId()));
        verifierDroit(besoin, Permission.ENGAGE_DEPENSE);
        if (besoin.getStatut() != StatutExpression.ATTRIBUEE) {
            throw new BusinessException(ErrorCode.BESOIN_NON_ATTRIBUE, besoin.getReference(), besoin.getStatut());
        }
        if (acteRepository.existsByExpressionBesoinId(besoin.getId())) {
            throw new BusinessException(ErrorCode.ACTE_DEJA_CREE, besoin.getReference());
        }
        final Soumission retenue = soumissionRepository
                .findByExpressionBesoinIdAndStatut(besoin.getId(), StatutSoumission.RETENUE)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUMISSION_NOT_FOUND, besoin.getReference()));
        final ActeJuridique acte = acteFactory.creerActe(besoin, retenue, request.type(), request.delaiLivraisonMois(),
                LocalDate.now(clock));
        acteRepository.save(acte);
        log.info("Projet d'acte {} créé pour le besoin {} ({} TTC)", acte.getNumero(), besoin.getReference(),
                acte.getMontantTTC());
        return acteMapper.toResponse(acte);
    }

    @Override
    @Transactional
    public ActeJuridiqueResponse modifier(String acteId, ModificationActeRequest request) {
        final ActeJuridique acte = findActeAvecDroit(acteId, Permission.ENGAGE_DEPENSE);
        acte.modifier(request.objet(), request.delaiLivraisonMois());
        log.info("Projet d'acte {} modifié", acte.getNumero());
        return acteMapper.toResponse(acte);
    }

    @Override
    @Transactional
    public ActeJuridiqueResponse soumettreAuControle(String acteId) {
        final ActeJuridique acte = findActeAvecDroit(acteId, Permission.ENGAGE_DEPENSE);
        final ExpressionBesoin b = acte.getExpressionBesoin();
        final CreditBudgetaire credit = creditRepository
                .findByExerciceIdAndSectionIdAndProgrammeIdAndActionIdAndChapitreIdAndNatureEconomiqueId(
                        b.getExercice().getId(), b.getSection().getId(), b.getProgramme().getId(),
                        b.getTache().getAction().getId(), b.getChapitre().getId(), b.getNatureEconomique().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CREDIT_IMPUTATION_INTROUVABLE, b.getReference()));
        acte.soumettreAuControle(credit, LocalDateTime.now(clock));
        log.info("Acte {} soumis au contrôle du CF (ligne {})", acte.getNumero(), credit.getCodeImputation());
        return acteMapper.toResponse(acte);
    }

    @Override
    public ActeJuridiqueResponse getActe(String acteId) {
        return acteMapper.toResponse(findActeAvecDroit(acteId, null));
    }

    @Override
    public PageResponse<ActeJuridiqueResponse> rechercher(String exerciceId, StatutActe statut, Pageable pageable) {
        final Perimetre perimetre = mandatScopeValidator.perimetreCourant();
        final Pageable page = Tris.verifier(pageable, TRIS_AUTORISES, Sort.by(Sort.Direction.DESC, "numero"));
        if (perimetre.isVide()) {
            return PageResponse.of(Page.empty(page), acteMapper::toResume);
        }
        return PageResponse.of(acteRepository.rechercherDansPerimetre(nonVide(perimetre.sectionsEntieres()),
                nonVide(perimetre.programmes()), exerciceId, statut, page), acteMapper::toResume);
    }

    // ── Interne ───────────────────────────────────────

    private ActeJuridique findActeAvecDroit(String acteId, Permission permission) {
        final ActeJuridique acte = acteRepository.findById(acteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTE_NOT_FOUND, acteId));
        verifierDroit(acte.getExpressionBesoin(), permission);
        return acte;
    }

    private void verifierDroit(ExpressionBesoin besoin, Permission permission) {
        mandatScopeValidator.checkAccessPerimetre(besoin.getSection().getId(), besoin.getProgramme().getId(), permission);
    }

    /** JPQL n'accepte pas « IN () » : une valeur qui ne correspond à aucun id remplace l'ensemble vide. */
    private static Set<String> nonVide(Set<String> ids) {
        return ids.isEmpty() ? Set.of(AUCUN_ID) : ids;
    }
}
