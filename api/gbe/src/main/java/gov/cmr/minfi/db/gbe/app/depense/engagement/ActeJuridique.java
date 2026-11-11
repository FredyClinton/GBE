package gov.cmr.minfi.db.gbe.app.depense.engagement;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.soumission.Soumission;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Acte juridique (UC04 à UC06, UC05) : « le projet d'acte et l'acte signé constituent le même objet à des stades
 * distincts de maturité » (fiche objet). Créé par {@link ActeJuridiqueFactory} à partir du besoin attribué et de
 * l'offre retenue, dont il reprend les montants (HT, TVA, IR, TTC, net à payer, plan §4.5).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "actes_juridiques")
public class ActeJuridique extends BaseEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TypeActe type;

    @Column(name = "objet", nullable = false, columnDefinition = "TEXT")
    private String objet;

    /** Date d'émission du BCA : date de création du projet (Q26). */
    @Column(name = "date_emission", nullable = false)
    private LocalDate dateEmission;

    /** Délai de livraison en mois (Q25), proposé à partir du délai de l'offre en jours. */
    @Column(name = "delai_livraison_mois", nullable = false)
    private Integer delaiLivraisonMois;

    @Column(name = "montant_total_ht", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantTotalHT;

    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTva;

    @Column(name = "montant_tva", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantTva;

    @Column(name = "taux_ir", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxIr;

    @Column(name = "montant_ir", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantIr;

    /** Montant engagé, « arrêté » en lettres et réservé sur les AE (Q17). */
    @Column(name = "montant_ttc", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantTTC;

    @Column(name = "montant_net_a_payer", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantNetAPayer;

    /** « Arrêté à la somme de » : le TTC en lettres. */
    @Column(name = "montant_en_lettres", nullable = false, columnDefinition = "TEXT")
    private String montantEnLettres;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private StatutActe statut = StatutActe.PROJET;

    @Column(name = "date_soumission_controle")
    private LocalDateTime dateSoumissionControle;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expression_besoin_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_acte_besoin"))
    private ExpressionBesoin expressionBesoin;

    /** Offre retenue (« titulaire retenu ») : fournisseur, RIB et lignes de l'acte. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soumission_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_acte_soumission"))
    private Soumission soumission;

    /** Ligne de crédit déduite de l'imputation du besoin à l'envoi au CF (Q22). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_budgetaire_id", foreignKey = @ForeignKey(name = "fk_acte_credit"))
    private CreditBudgetaire creditBudgetaire;

    // ── Cycle de vie ──────────────────────────────────

    /** Le projet reste modifiable par l'ordonnateur tant qu'il n'est pas soumis au CF (UC04). */
    public void modifier(String nouvelObjet, Integer nouveauDelaiMois) {
        if (statut != StatutActe.PROJET) {
            throw new BusinessException(ErrorCode.ACTE_NON_MODIFIABLE, numero, statut);
        }
        if (nouveauDelaiMois == null || nouveauDelaiMois <= 0) {
            throw new BusinessException(ErrorCode.MONTANT_INVALIDE);
        }
        this.objet = nouvelObjet;
        this.delaiLivraisonMois = nouveauDelaiMois;
    }

    /** PROJET → SOUMIS_CONTROLE (soumettreAuVisa, transmettreAuCF de l'onglet 2) : plus de modification unilatérale. */
    public void soumettreAuControle(CreditBudgetaire credit, LocalDateTime maintenant) {
        validerTransition(Set.of(StatutActe.PROJET), StatutActe.SOUMIS_CONTROLE,
                "Seul un projet d'acte peut être soumis au contrôle du CF");
        this.creditBudgetaire = credit;
        this.statut = StatutActe.SOUMIS_CONTROLE;
        this.dateSoumissionControle = maintenant;
    }

    private void validerTransition(Set<StatutActe> autorises, StatutActe cible, String message) {
        if (!autorises.contains(statut)) {
            throw new BusinessException(ErrorCode.TRANSITION_STATUT_INVALIDE, statut, cible, message);
        }
    }
}
