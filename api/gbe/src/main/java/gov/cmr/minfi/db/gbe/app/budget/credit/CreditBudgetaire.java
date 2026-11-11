package gov.cmr.minfi.db.gbe.app.budget.credit;

import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomique;
import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Chapitre;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Action;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(
        name = "CREDIT_BUDGETAIRE",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_credit_imputation",
                // Doit rester aligné sur CreditBudgetaireServiceImpl.validateImputationUniqueness
                // et sur la migration V5__nature_economique_credit.sql (NULLS NOT DISTINCT en base)
                columnNames = {
                        "EXERCICE_ID", "SECTION_ID", "PROGRAMME_ID",
                        "ACTION_ID", "CHAPITRE_ID", "NATURE_ECONOMIQUE_ID"
                }
        )
)
public class CreditBudgetaire extends BaseEntity
        implements AEManageable, CPManageable, StatutTransitionable {

    // ── Classification administrative ─────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXERCICE_ID", nullable = false)
    private Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECTION_ID", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROGRAMME_ID", nullable = false)
    private Programme programme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTION_ID", nullable = false)
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAPITRE_ID", nullable = false)
    private Chapitre chapitre;

    // ── Classification économique ─────────────────────
    // Obligatoire à la création ; NULL uniquement pour les crédits créés avant V5.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NATURE_ECONOMIQUE_ID",
            foreignKey = @ForeignKey(name = "fk_credit_nature_economique"))
    private NatureEconomique natureEconomique;

    @Column(name = "CODE_IMPUTATION", nullable = false, unique = true, length = 50)
    private String codeImputation;

    // ── AE ────────────────────────────────────────────
    @Column(name = "MONTANT_AE", nullable = false, precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal montantAE = BigDecimal.ZERO;

    @Column(name = "MONTANT_AE_CONSOMME", nullable = false, precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal montantAEConsomme = BigDecimal.ZERO;

    @Column(name = "MONTANT_AE_DISPONIBLE", nullable = false, precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal montantAEDisponible = BigDecimal.ZERO;

    // ── CP ────────────────────────────────────────────
    @Column(name = "MONTANT_CP", nullable = false, precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal montantCP = BigDecimal.ZERO;

    @Column(name = "MONTANT_CP_CONSOMME", nullable = false, precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal montantCPConsomme = BigDecimal.ZERO;

    @Column(name = "MONTANT_CP_DISPONIBLE", nullable = false, precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal montantCPDisponible = BigDecimal.ZERO;

    // ── Statut ────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT", nullable = false)
    @Builder.Default
    private StatutCredit statut = StatutCredit.DISPONIBLE;

    // ── AE pluriannuelle (auto-référence) ─────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREDIT_PARENT_ID")
    private CreditBudgetaire creditParent;

    // ── Initialisation des disponibles ────────────────
    @PrePersist
    private void initialiserDisponibles() {
        this.montantAEDisponible = this.montantAE;
        this.montantCPDisponible = this.montantCP;
    }

    // ── AEManageable ──────────────────────────────────
    // Module Dépense : les AE sont consommées à la réservation (visa CF de l'engagement)
    // et libérées à l'annulation d'un acte déjà visé. Une ligne finance plusieurs dépenses.
    @Override
    public void consommerAE(BigDecimal montant) {
        verifierMontantPositif(montant);
        verifierConsommable();
        if (montant.compareTo(this.montantAEDisponible) > 0) {
            throw new BusinessException(ErrorCode.MONTANT_AE_INSUFFISANT);
        }
        this.montantAEConsomme = this.montantAEConsomme.add(montant);
        this.montantAEDisponible = this.montantAEDisponible.subtract(montant);
    }

    @Override
    public void libererAE(BigDecimal montant) {
        verifierMontantPositif(montant);
        if (montant.compareTo(this.montantAEConsomme) > 0) {
            throw new BusinessException(ErrorCode.MONTANT_LIBERE_SUPERIEUR_CONSOMME, "AE");
        }
        this.montantAEConsomme = this.montantAEConsomme.subtract(montant);
        this.montantAEDisponible = this.montantAEDisponible.add(montant);
    }

    // ── CPManageable ──────────────────────────────────
    // Module Dépense : les CP sont consommés à l'ordonnancement.
    @Override
    public void consommerCP(BigDecimal montant) {
        verifierMontantPositif(montant);
        verifierConsommable();
        if (montant.compareTo(this.montantCPDisponible) > 0) {
            throw new BusinessException(ErrorCode.MONTANT_CP_INSUFFISANT);
        }
        this.montantCPConsomme = this.montantCPConsomme.add(montant);
        this.montantCPDisponible = this.montantCPDisponible.subtract(montant);
    }

    @Override
    public void libererCP(BigDecimal montant) {
        verifierMontantPositif(montant);
        if (montant.compareTo(this.montantCPConsomme) > 0) {
            throw new BusinessException(ErrorCode.MONTANT_LIBERE_SUPERIEUR_CONSOMME, "CP");
        }
        this.montantCPConsomme = this.montantCPConsomme.subtract(montant);
        this.montantCPDisponible = this.montantCPDisponible.add(montant);
    }

    /** Seul un crédit DISPONIBLE peut être consommé (cantonné, suspendu ou annulé : bloqué). */
    public boolean isConsommable() {
        return this.statut == StatutCredit.DISPONIBLE;
    }

    private void verifierConsommable() {
        if (!isConsommable()) {
            throw new BusinessException(ErrorCode.CREDIT_NON_CONSOMMABLE, this.statut);
        }
    }

    private static void verifierMontantPositif(BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) {
            throw new BusinessException(ErrorCode.MONTANT_INVALIDE);
        }
    }

    // ── StatutTransitionable ──────────────────────────

    // DISPONIBLE → SUSPENDU
    @Override
    public void suspendre() {
        validerTransition(
                Set.of(StatutCredit.DISPONIBLE),
                StatutCredit.SUSPENDU,
                "Seul un crédit DISPONIBLE peut être suspendu"
        );
        this.statut = StatutCredit.SUSPENDU;
    }

    // SUSPENDU → DISPONIBLE
    @Override
    public void debloquer() {
        validerTransition(
                Set.of(StatutCredit.SUSPENDU),
                StatutCredit.DISPONIBLE,
                "Seul un crédit SUSPENDU peut être débloqué"
        );
        this.statut = StatutCredit.DISPONIBLE;
    }

    // DISPONIBLE → CANTONNE
    @Override
    public void cantonner() {
        validerTransition(
                Set.of(StatutCredit.DISPONIBLE),
                StatutCredit.CANTONNE,
                "Seul un crédit DISPONIBLE peut être cantonné"
        );
        this.statut = StatutCredit.CANTONNE;
    }

    // CANTONNE → DISPONIBLE
    @Override
    public void decantionner() {
        validerTransition(
                Set.of(StatutCredit.CANTONNE),
                StatutCredit.DISPONIBLE,
                "Seul un crédit CANTONNE peut être décantonné"
        );
        this.statut = StatutCredit.DISPONIBLE;
    }

    // DISPONIBLE → ANNULE
    // SUSPENDU   → ANNULE
    // CANTONNE   → ANNULE
    // Refusé tant que des AE ou des CP sont consommés : il faut d'abord annuler les actes qui les portent.
    @Override
    public void annuler() {
        validerTransition(
                Set.of(StatutCredit.DISPONIBLE, StatutCredit.SUSPENDU, StatutCredit.CANTONNE),
                StatutCredit.ANNULE,
                "Un crédit déjà ANNULÉ ne peut pas être annulé"
        );
        if (this.montantAEConsomme.signum() > 0 || this.montantCPConsomme.signum() > 0) {
            throw new BusinessException(ErrorCode.CREDIT_ANNULATION_CREDITS_CONSOMMES,
                    this.montantAEConsomme, this.montantCPConsomme);
        }
        this.statut = StatutCredit.ANNULE;
    }

    // ── Helper : validation générique des transitions ─
    private void validerTransition(
            Set<StatutCredit> statutsAutorises,
            StatutCredit cible,
            String messageErreur) {
        if (!statutsAutorises.contains(this.statut)) {
            throw new BusinessException(
                    ErrorCode.TRANSITION_STATUT_INVALIDE,
                    this.statut, cible, messageErreur
            );
        }
    }

    // ── Helper métier ─────────────────────────────────
    public boolean isAEPluriannuelle() {
        return this.creditParent != null;
    }

    public boolean isTerminal() {
        return this.statut == StatutCredit.ANNULE;
    }
}
