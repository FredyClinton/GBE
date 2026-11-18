package gov.cmr.minfi.db.gbe.app.budget.depense.engagement;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.budget.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.BonDeCommande;
import gov.cmr.minfi.db.gbe.app.budget.depense.soumission.Soumission;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
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

    /** Ordonnateur signataire (validation tracée, Q6). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signataire_id", foreignKey = @ForeignKey(name = "fk_acte_signataire"))
    private User signataire;

    @Column(name = "date_signature")
    private LocalDateTime dateSignature;

    /** Mise à disposition du fournisseur dans son espace (Q20) ; reste renseignée si l'acte est annulé ensuite. */
    @Column(name = "date_notification")
    private LocalDateTime dateNotification;

    @Column(name = "motif_annulation", columnDefinition = "TEXT")
    private String motifAnnulation;

    @Column(name = "date_annulation")
    private LocalDateTime dateAnnulation;

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

    /**
     * Données de la forme BCA (« applique forme contractuelle », composition). Les autres formes auront leur propre
     * entité ; une seule est renseignée, selon {@link #type}.
     */
    @OneToOne(mappedBy = "acteJuridique", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BonDeCommande bonDeCommande;

    /** Réservation des AE créée par le visa favorable (« bloque crédits » 0..1). */
    @OneToOne(mappedBy = "acteJuridique", fetch = FetchType.LAZY)
    private ReservationCredit reservation;

    // ── Cycle de vie ──────────────────────────────────

    public void rattacherBonDeCommande(BonDeCommande bca) {
        bca.setActeJuridique(this);
        this.bonDeCommande = bca;
    }

    /** Le projet reste modifiable par l'ordonnateur tant qu'il n'est pas soumis au CF (UC04). */
    public void verifierModifiable() {
        if (statut != StatutActe.PROJET) {
            throw new BusinessException(ErrorCode.ACTE_NON_MODIFIABLE, numero, statut);
        }
    }

    /** Modifie l'objet ; les données de la forme se modifient par sa stratégie. */
    public void modifier(String nouvelObjet) {
        verifierModifiable();
        this.objet = nouvelObjet;
    }

    /** PROJET → SOUMIS_CONTROLE (soumettreAuVisa, transmettreAuCF de l'onglet 2) : plus de modification unilatérale. */
    public void soumettreAuControle(CreditBudgetaire credit, LocalDateTime maintenant) {
        validerTransition(Set.of(StatutActe.PROJET), StatutActe.SOUMIS_CONTROLE,
                "Seul un projet d'acte peut être soumis au contrôle du CF");
        this.creditBudgetaire = credit;
        this.statut = StatutActe.SOUMIS_CONTROLE;
        this.dateSoumissionControle = maintenant;
    }

    /** SOUMIS_CONTROLE → VISE_CF (viser(ACCORDE) de l'onglet 2 : quitus et crédits accordés). */
    public void viser() {
        validerTransition(Set.of(StatutActe.SOUMIS_CONTROLE), StatutActe.VISE_CF,
                "Seul un acte soumis au contrôle peut être visé");
        this.statut = StatutActe.VISE_CF;
    }

    /**
     * SOUMIS_CONTROLE → PROJET (rejeter(motif) de l'onglet 2, « refus / rejet » de l'onglet 3, Q18) : le dossier revient
     * à l'ordonnateur pour correction ; la ligne de crédit sera de nouveau déduite au prochain envoi.
     */
    public void renvoyerEnProjet() {
        validerTransition(Set.of(StatutActe.SOUMIS_CONTROLE), StatutActe.PROJET,
                "Seul un acte soumis au contrôle peut être rejeté");
        this.statut = StatutActe.PROJET;
        this.creditBudgetaire = null;
    }

    /** Réserves du CF (Q18) : l'acte reste soumis au contrôle, en attente de compléments. */
    public void verifierSoumisAuControle() {
        validerTransition(Set.of(StatutActe.SOUMIS_CONTROLE), StatutActe.SOUMIS_CONTROLE,
                "Seul un acte soumis au contrôle peut recevoir une décision du CF");
    }

    /**
     * VISE_CF → SIGNE_ORDONNATEUR (ordonnateur.signer() de l'onglet 2, UC05) : validation tracée, signataire et date
     * (Q6). Le service enchaîne aussitôt {@link #notifierFournisseur} (Q20).
     */
    public void signer(User ordonnateur, LocalDateTime maintenant) {
        validerTransition(Set.of(StatutActe.VISE_CF), StatutActe.SIGNE_ORDONNATEUR,
                "Seul un acte visé par le CF peut être signé");
        this.signataire = ordonnateur;
        this.dateSignature = maintenant;
        this.statut = StatutActe.SIGNE_ORDONNATEUR;
    }

    /** SIGNE_ORDONNATEUR → NOTIFIE_PRESTATAIRE (notifierFournisseur() de l'onglet 2) : l'exécution peut commencer. */
    public void notifierFournisseur(LocalDateTime maintenant) {
        validerTransition(Set.of(StatutActe.SIGNE_ORDONNATEUR), StatutActe.NOTIFIE_PRESTATAIRE,
                "Seul un acte signé peut être notifié au fournisseur");
        this.dateNotification = maintenant;
        this.statut = StatutActe.NOTIFIE_PRESTATAIRE;
    }

    /**
     * VISE_CF | SIGNE_ORDONNATEUR | NOTIFIE_PRESTATAIRE → ANNULE (Q2) : le service annule la réservation et libère
     * les AE. Un projet ou un acte soumis au CF ne s'annule pas (rien n'est réservé, décision à prendre).
     */
    public void annuler(String motif, LocalDateTime maintenant) {
        validerTransition(Set.of(StatutActe.VISE_CF, StatutActe.SIGNE_ORDONNATEUR, StatutActe.NOTIFIE_PRESTATAIRE),
                StatutActe.ANNULE, "Seul un acte visé, signé ou notifié peut être annulé");
        this.motifAnnulation = motif;
        this.dateAnnulation = maintenant;
        this.statut = StatutActe.ANNULE;
    }

    /** L'acte a été mis à disposition du fournisseur (il le voit dans son espace, même annulé ensuite). */
    public boolean isNotifie() {
        return dateNotification != null;
    }

    private void validerTransition(Set<StatutActe> autorises, StatutActe cible, String message) {
        if (!autorises.contains(statut)) {
            throw new BusinessException(ErrorCode.TRANSITION_STATUT_INVALIDE, statut, cible, message);
        }
    }
}
