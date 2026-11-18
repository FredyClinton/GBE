package gov.cmr.minfi.db.gbe.app.budget.depense.engagement.impl;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaireRepository;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.common.pagination.Tris;
import gov.cmr.minfi.db.gbe.app.budget.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.budget.depense.besoin.ExpressionBesoinRepository;
import gov.cmr.minfi.db.gbe.app.budget.depense.besoin.StatutExpression;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.*;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.dto.ActeJuridiqueResponse;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.dto.CreationActeRequest;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.dto.ModificationActeRequest;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.DonneesForme;
import gov.cmr.minfi.db.gbe.app.budget.depense.soumission.Soumission;
import gov.cmr.minfi.db.gbe.app.budget.depense.soumission.SoumissionRepository;
import gov.cmr.minfi.db.gbe.app.budget.depense.soumission.StatutSoumission;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.Fournisseur;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.FournisseurConnecte;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatScopeValidator;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatScopeValidator.Perimetre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
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
    static final Set<String> TRIS_FOURNISSEUR = Set.of("numero", "dateNotification", "montantTTC", "statut");
    private static final String AUCUN_ID = "-";

    private final ActeJuridiqueRepository acteRepository;
    private final ExpressionBesoinRepository besoinRepository;
    private final SoumissionRepository soumissionRepository;
    private final CreditBudgetaireRepository creditRepository;
    private final ActeJuridiqueFactory acteFactory;
    private final MandatScopeValidator mandatScopeValidator;
    private final ActeJuridiqueMapper acteMapper;
    private final FournisseurConnecte fournisseurConnecte;
    private final ApplicationEventPublisher evenements;
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
        final ActeJuridique acte = acteFactory.creerActe(besoin, retenue, request.type(),
                new DonneesForme(request.delaiLivraisonMois(), request.referenceDevis()), LocalDate.now(clock));
        acteRepository.save(acte);
        log.info("Projet d'acte {} créé pour le besoin {} ({} TTC)", acte.getNumero(), besoin.getReference(),
                acte.getMontantTTC());
        return acteMapper.toResponse(acte);
    }

    @Override
    @Transactional
    public ActeJuridiqueResponse modifier(String acteId, ModificationActeRequest request) {
        final ActeJuridique acte = findActeAvecDroit(acteId, Permission.ENGAGE_DEPENSE);
        acte.modifier(request.objet());
        acteFactory.forme(acte.getType()).appliquerDonnees(acte, acte.getSoumission(),
                new DonneesForme(request.delaiLivraisonMois(), request.referenceDevis()));
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
    @Transactional
    public ActeJuridiqueResponse signer(String acteId) {
        final ActeJuridique acte = findActeAvecDroit(acteId, Permission.SIGNER_ACTE);
        final LocalDateTime maintenant = LocalDateTime.now(clock);
        acte.signer(ordonnateurConnecte(), maintenant);
        // UC05, étape 3 : « le système notifie immédiatement l'acte officiel au fournisseur » (Q20)
        acte.notifierFournisseur(maintenant);
        evenements.publishEvent(new ActeNotifieEvent(acte.getId(), acte.getNumero(), acte.getObjet(),
                acte.getSoumission().getFournisseur().getUser().getId()));
        log.info("Acte {} signé et notifié au fournisseur {}", acte.getNumero(),
                acte.getSoumission().getFournisseur().getNiu());
        return acteMapper.toResponse(acte);
    }

    @Override
    @Transactional
    public ActeJuridiqueResponse annuler(String acteId, String motif) {
        final ActeJuridique acte = findActeAvecDroit(acteId, Permission.ENGAGE_DEPENSE);
        if (motif == null || motif.isBlank()) {
            throw new BusinessException(ErrorCode.ACTE_MOTIF_ANNULATION_OBLIGATOIRE, acte.getNumero());
        }
        final LocalDateTime maintenant = LocalDateTime.now(clock);
        acte.annuler(motif.trim(), maintenant);
        // Un acte visé a toujours une réservation active (2c) : ses AE reviennent sur la ligne
        if (acte.getReservation() != null) {
            acte.getReservation().annuler(maintenant);
        }
        evenements.publishEvent(new ActeAnnuleEvent(acte.getId(), acte.getNumero(), acte.getMotifAnnulation(),
                acte.isNotifie() ? acte.getSoumission().getFournisseur().getUser().getId() : null));
        log.info("Acte {} annulé ({} libérés sur les AE)", acte.getNumero(),
                acte.getReservation() == null ? 0 : acte.getReservation().getMontant());
        return acteMapper.toResponse(acte);
    }

    @Override
    public PageResponse<ActeJuridiqueResponse> mesActesFournisseur(Pageable pageable) {
        final Fournisseur fournisseur = fournisseurConnecte.exiger();
        final Pageable page = Tris.verifier(pageable, TRIS_FOURNISSEUR, Sort.by(Sort.Direction.DESC, "dateNotification"));
        return PageResponse.of(acteRepository.findNotifiesAuFournisseur(fournisseur.getId(), page),
                acteMapper::toActeFournisseurResume);
    }

    @Override
    public ActeJuridiqueResponse getActeFournisseur(String acteId) {
        final Fournisseur fournisseur = fournisseurConnecte.exiger();
        // L'acte d'un autre fournisseur, ou pas encore notifié, est introuvable
        return acteMapper.toActeFournisseur(acteRepository.findNotifieAuFournisseur(acteId, fournisseur.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTE_NOT_FOUND, acteId)));
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

    private static User ordonnateurConnecte() {
        return (User) java.util.Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication())
                .getPrincipal();
    }

    /** JPQL n'accepte pas « IN () » : une valeur qui ne correspond à aucun id remplace l'ensemble vide. */
    private static Set<String> nonVide(Set<String> ids) {
        return ids.isEmpty() ? Set.of(AUCUN_ID) : ids;
    }
}
