package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Chapitre;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomique;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.Matiere;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Tache;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Expression de besoin (UC01) : exprimée par l'ordonnateur, sans prix, puis publiée aux fournisseurs.
 * <ul>
 *   <li>BROUILLON → PUBLIEE ({@link #publier}) → EN_EVALUATION ({@link #cloturerSoumissions}, après la date limite)</li>
 *   <li>PUBLIEE → ANNULEE ({@link #annuler}) ; un BROUILLON est supprimé logiquement ({@link #supprimer})</li>
 *   <li>modifiable en BROUILLON, et en PUBLIEE tant qu'aucune soumission n'est déposée et que la date limite
 *       n'est pas passée (décision 2026-11-11)</li>
 * </ul>
 * Exercice, section et programme sont recopiés depuis la tâche.
 * Les besoins supprimés logiquement sont exclus de toutes les requêtes ({@link SQLRestriction}).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "expressions_besoin")
@SQLRestriction("supprime = false")
public class ExpressionBesoin extends BaseEntity {

    @Column(name = "reference", nullable = false, unique = true, length = 30)
    private String reference;

    @Column(name = "objet", nullable = false, columnDefinition = "TEXT")
    private String objet;

    @Column(name = "date_expression", nullable = false)
    private LocalDate dateExpression;

    @Column(name = "date_cloture_soumission")
    private LocalDateTime dateClotureSoumission;

    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    @Column(name = "date_cloture_effective")
    private LocalDateTime dateClotureEffective;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private StatutExpression statut = StatutExpression.BROUILLON;

    @Column(name = "motif_annulation", columnDefinition = "TEXT")
    private String motifAnnulation;

    @Column(name = "supprime", nullable = false)
    @Builder.Default
    private boolean supprime = false;

    // ── Rattachements ─────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tache_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expression_besoin_tache"))
    private Tache tache;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expression_besoin_programme"))
    private Programme programme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expression_besoin_section"))
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expression_besoin_exercice"))
    private Exercice exercice;

    /** Structure émettrice. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapitre_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expression_besoin_chapitre"))
    private Chapitre chapitre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nature_economique_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_expression_besoin_nature"))
    private NatureEconomique natureEconomique;

    @OneToMany(mappedBy = "expressionBesoin", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroOrdre ASC")
    @Builder.Default
    private List<LigneBesoin> lignes = new ArrayList<>();

    // ── Rattachement ──────────────────────────────────

    /** Rattache le besoin à une tâche et recopie programme, section et exercice. */
    public void rattacherA(Tache tache) {
        this.tache = tache;
        this.programme = tache.getProgramme();
        this.section = tache.getSection();
        this.exercice = tache.getExercice();
    }

    // ── Lignes ────────────────────────────────────────

    public void viderLignes() {
        this.lignes.clear();
    }

    /** Ajoute une ligne numérotée à la suite ; une matière ne figure qu'une fois. */
    public LigneBesoin ajouterLigne(Matiere matiere, BigDecimal quantite, String specifications) {
        if (lignes.stream().anyMatch(l -> l.getMatiere().getId().equals(matiere.getId()))) {
            throw new BusinessException(ErrorCode.BESOIN_MATIERE_EN_DOUBLE, matiere.getReference());
        }
        if (quantite == null || quantite.signum() <= 0) {
            throw new BusinessException(ErrorCode.MONTANT_INVALIDE);
        }
        final LigneBesoin ligne = LigneBesoin.builder()
                .numeroOrdre(lignes.size() + 1)
                .matiere(matiere)
                .quantiteDemandee(quantite)
                .specifications(specifications)
                .expressionBesoin(this)
                .build();
        lignes.add(ligne);
        return ligne;
    }

    // ── Cycle de vie ──────────────────────────────────

    /**
     * @param soumissionsDeposees vrai dès qu'une soumission existe sur ce besoin (branché au lot 1e)
     */
    public void verifierModifiable(LocalDateTime maintenant, boolean soumissionsDeposees) {
        final boolean modifiable = statut == StatutExpression.BROUILLON
                || (statut == StatutExpression.PUBLIEE && !soumissionsDeposees && isAvantDateLimite(maintenant));
        if (!modifiable) {
            throw new BusinessException(ErrorCode.BESOIN_NON_MODIFIABLE, reference, statut);
        }
    }

    /** Conditions pour être (ou rester) visible des fournisseurs. */
    public void verifierPublicable(LocalDateTime maintenant) {
        if (lignes.isEmpty()) {
            throw new BusinessException(ErrorCode.BESOIN_SANS_LIGNE);
        }
        if (!isAvantDateLimite(maintenant)) {
            throw new BusinessException(ErrorCode.BESOIN_DATE_CLOTURE_INVALIDE);
        }
    }

    // BROUILLON → PUBLIEE
    public void publier(LocalDateTime maintenant) {
        validerTransition(Set.of(StatutExpression.BROUILLON), StatutExpression.PUBLIEE,
                "Seul un besoin en BROUILLON peut être publié");
        verifierPublicable(maintenant);
        this.statut = StatutExpression.PUBLIEE;
        this.datePublication = maintenant;
    }

    // PUBLIEE → EN_EVALUATION, une fois la date limite passée
    public void cloturerSoumissions(LocalDateTime maintenant) {
        validerTransition(Set.of(StatutExpression.PUBLIEE), StatutExpression.EN_EVALUATION,
                "Seul un besoin PUBLIÉ peut être clôturé");
        if (isAvantDateLimite(maintenant)) {
            throw new BusinessException(ErrorCode.BESOIN_CLOTURE_PREMATUREE, dateClotureSoumission);
        }
        this.statut = StatutExpression.EN_EVALUATION;
        this.dateClotureEffective = maintenant;
    }

    // PUBLIEE → ANNULEE
    public void annuler(String motif) {
        validerTransition(Set.of(StatutExpression.PUBLIEE), StatutExpression.ANNULEE,
                "Seul un besoin PUBLIÉ peut être annulé (un brouillon se supprime)");
        this.statut = StatutExpression.ANNULEE;
        this.motifAnnulation = motif;
    }

    /** Suppression logique, réservée aux brouillons. */
    public void supprimer() {
        if (statut != StatutExpression.BROUILLON) {
            throw new BusinessException(ErrorCode.TRANSITION_STATUT_INVALIDE, statut, "supprimé",
                    "Seul un BROUILLON peut être supprimé (un besoin publié s'annule)");
        }
        this.supprime = true;
    }

    /** Publié et date limite non atteinte : les fournisseurs peuvent consulter et déposer. */
    public boolean isOuverteAuxSoumissions(LocalDateTime maintenant) {
        return statut == StatutExpression.PUBLIEE && isAvantDateLimite(maintenant);
    }

    private boolean isAvantDateLimite(LocalDateTime maintenant) {
        return dateClotureSoumission != null && maintenant.isBefore(dateClotureSoumission);
    }

    private void validerTransition(Set<StatutExpression> autorises, StatutExpression cible, String message) {
        if (!autorises.contains(statut)) {
            throw new BusinessException(ErrorCode.TRANSITION_STATUT_INVALIDE, statut, cible, message);
        }
    }
}
