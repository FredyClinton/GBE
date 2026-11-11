package gov.cmr.minfi.db.gbe.app.depense.besoin.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.common.pagination.Tris;
import gov.cmr.minfi.db.gbe.app.common.reference.ReferenceGenerator;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoinMapper;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoinRepository;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoinService;
import gov.cmr.minfi.db.gbe.app.depense.besoin.StatutExpression;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.AnnulationBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinResponse;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.LigneBesoinRequest;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatScopeValidator;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatScopeValidator.Perimetre;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Chapitre;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.ChapitreRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomique;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomiqueRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.Matiere;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.MatiereRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Tache;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.TacheRepository;
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
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpressionBesoinServiceImpl implements ExpressionBesoinService {

    static final String PREFIXE_REFERENCE = "EB";
    static final Set<String> TRIS_AUTORISES =
            Set.of("reference", "dateExpression", "datePublication", "dateClotureSoumission", "statut");
    private static final String AUCUN_ID = "-";

    private final ExpressionBesoinRepository besoinRepository;
    private final TacheRepository tacheRepository;
    private final ChapitreRepository chapitreRepository;
    private final NatureEconomiqueRepository natureEconomiqueRepository;
    private final MatiereRepository matiereRepository;
    private final MandatScopeValidator mandatScopeValidator;
    private final ReferenceGenerator referenceGenerator;
    private final ExpressionBesoinMapper besoinMapper;
    private final Clock clock;

    @Override
    @Transactional
    public ExpressionBesoinResponse creer(ExpressionBesoinRequest request) {
        final ExpressionBesoin besoin = ExpressionBesoin.builder()
                .dateExpression(LocalDate.now(clock))
                .build();
        appliquer(besoin, request);
        besoin.setReference(referenceGenerator.prochaineReference(PREFIXE_REFERENCE, besoin.getExercice().getAnnee()));
        besoinRepository.save(besoin);
        log.info("Expression de besoin créée : {} ({} ligne(s))", besoin.getReference(), besoin.getLignes().size());
        return besoinMapper.toResponse(besoin);
    }

    @Override
    @Transactional
    public ExpressionBesoinResponse modifier(String besoinId, ExpressionBesoinRequest request) {
        final ExpressionBesoin besoin = findBesoinAvecDroit(besoinId);
        besoin.verifierModifiable(maintenant(), soumissionsDeposees(besoin));
        besoin.viderLignes();
        besoinRepository.flush(); // supprime les anciennes lignes avant d'insérer les nouvelles (contraintes d'unicité)
        appliquer(besoin, request);
        if (besoin.getStatut() == StatutExpression.PUBLIEE) {
            besoin.verifierPublicable(maintenant()); // un besoin publié doit rester complet et ouvert
        }
        log.info("Expression de besoin modifiée : {}", besoin.getReference());
        return besoinMapper.toResponse(besoin);
    }

    @Override
    @Transactional
    public ExpressionBesoinResponse publier(String besoinId) {
        final ExpressionBesoin besoin = findBesoinAvecDroit(besoinId);
        besoin.publier(maintenant());
        log.info("Expression de besoin publiée : {} (date limite {})", besoin.getReference(),
                besoin.getDateClotureSoumission());
        return besoinMapper.toResponse(besoin);
    }

    @Override
    @Transactional
    public ExpressionBesoinResponse cloturerSoumissions(String besoinId) {
        final ExpressionBesoin besoin = findBesoinAvecDroit(besoinId);
        besoin.cloturerSoumissions(maintenant());
        log.info("Soumissions clôturées : {}", besoin.getReference());
        return besoinMapper.toResponse(besoin);
    }

    @Override
    @Transactional
    public ExpressionBesoinResponse annuler(String besoinId, AnnulationBesoinRequest request) {
        final ExpressionBesoin besoin = findBesoinAvecDroit(besoinId);
        besoin.annuler(request.motif());
        log.info("Expression de besoin annulée : {}", besoin.getReference());
        return besoinMapper.toResponse(besoin);
    }

    @Override
    @Transactional
    public void supprimer(String besoinId) {
        final ExpressionBesoin besoin = findBesoinAvecDroit(besoinId);
        besoin.supprimer();
        log.info("Brouillon supprimé (logiquement) : {}", besoin.getReference());
    }

    @Override
    public ExpressionBesoinResponse getBesoin(String besoinId) {
        final ExpressionBesoin besoin = findBesoin(besoinId);
        mandatScopeValidator.checkAccessPerimetre(besoin.getSection().getId(), besoin.getProgramme().getId(), null);
        return besoinMapper.toResponse(besoin);
    }

    @Override
    public PageResponse<ExpressionBesoinResponse> rechercher(String exerciceId, String sectionId, String programmeId,
                                                             StatutExpression statut, Pageable pageable) {
        final Perimetre perimetre = mandatScopeValidator.perimetreCourant();
        final Pageable page = Tris.verifier(pageable, TRIS_AUTORISES, Sort.by(Sort.Direction.DESC, "reference"));
        if (perimetre.isVide()) {
            return PageResponse.of(Page.empty(page), besoinMapper::toResume);
        }
        return PageResponse.of(besoinRepository.rechercherDansPerimetre(
                nonVide(perimetre.sectionsEntieres()), nonVide(perimetre.programmes()),
                exerciceId, sectionId, programmeId, statut, page), besoinMapper::toResume);
    }

    /** JPQL n'accepte pas « IN () » : une valeur qui ne correspond à aucun id remplace l'ensemble vide. */
    private static Set<String> nonVide(Set<String> ids) {
        return ids.isEmpty() ? Set.of(AUCUN_ID) : ids;
    }

    // ── Interne ───────────────────────────────────────

    /** Applique la requête : rattachements (avec contrôle du périmètre sur la tâche cible) et lignes. */
    private void appliquer(ExpressionBesoin besoin, ExpressionBesoinRequest request) {
        final Tache tache = tacheRepository.findById(request.tacheId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TACHE_NOT_FOUND, request.tacheId()));
        mandatScopeValidator.checkAccessPerimetre(
                tache.getSection() == null ? null : tache.getSection().getId(),
                tache.getProgramme().getId(), Permission.EXPRIMER_BESOIN);

        final Chapitre chapitre = chapitreRepository.findById(request.chapitreId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, request.chapitreId()));
        if (chapitre.getSection() == null || !chapitre.getSection().getId().equals(tache.getSection().getId())) {
            throw new BusinessException(ErrorCode.BESOIN_CHAPITRE_HORS_SECTION, chapitre.getCodeComplet());
        }

        final NatureEconomique nature = natureEconomiqueRepository.findById(request.natureEconomiqueId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, request.natureEconomiqueId()));
        if (nature.getExercice() != null && !nature.getExercice().getId().equals(tache.getExercice().getId())) {
            throw new BusinessException(ErrorCode.NATURE_ECONOMIQUE_NOT_IN_EXERCICE, nature.getCode());
        }

        besoin.setObjet(request.objet());
        besoin.setDateClotureSoumission(request.dateClotureSoumission());
        besoin.rattacherA(tache);
        besoin.setChapitre(chapitre);
        besoin.setNatureEconomique(nature);

        if (request.lignes() != null) {
            for (LigneBesoinRequest ligne : request.lignes()) {
                final Matiere matiere = matiereRepository.findById(ligne.matiereId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.MATIERE_NOT_FOUND, ligne.matiereId()));
                besoin.ajouterLigne(matiere, ligne.quantiteDemandee(), ligne.specifications());
            }
        }
    }

    /** Charge le besoin et vérifie que l'utilisateur peut agir dessus (périmètre + EXPRIMER_BESOIN). */
    private ExpressionBesoin findBesoinAvecDroit(String besoinId) {
        final ExpressionBesoin besoin = findBesoin(besoinId);
        mandatScopeValidator.checkAccessPerimetre(besoin.getSection().getId(), besoin.getProgramme().getId(),
                Permission.EXPRIMER_BESOIN);
        return besoin;
    }

    private ExpressionBesoin findBesoin(String besoinId) {
        return besoinRepository.findById(besoinId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BESOIN_NOT_FOUND, besoinId));
    }

    /** Branché à l'étape 1e (soumissions) : tant qu'il n'existe pas de soumission, toujours faux. */
    private boolean soumissionsDeposees(ExpressionBesoin besoin) {
        return false;
    }

    private LocalDateTime maintenant() {
        return LocalDateTime.now(clock);
    }
}
