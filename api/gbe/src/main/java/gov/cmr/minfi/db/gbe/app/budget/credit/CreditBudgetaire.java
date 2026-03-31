package gov.cmr.minfi.db.gbe.app.budget.credit;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
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
                columnNames = {
                        "EXERCICE_ID", "SECTION_ID",
                        "PROGRAMME_ID", "ACTION_ID", "CHAPITRE_ID"
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
    @Override
    public void consommerAE(BigDecimal montant) {
        if (montant.compareTo(this.montantAEDisponible) > 0) {
            throw new BusinessException(ErrorCode.MONTANT_AE_INSUFFISANT);
        }
        this.montantAEConsomme = this.montantAEConsomme.add(montant);
        this.montantAEDisponible = this.montantAEDisponible.subtract(montant);
    }

    @Override
    public void libererAE(BigDecimal montant) {
        this.montantAEConsomme = this.montantAEConsomme.subtract(montant);
        this.montantAEDisponible = this.montantAEDisponible.add(montant);
    }

    // ── CPManageable ──────────────────────────────────
    @Override
    public void consommerCP(BigDecimal montant) {
        if (montant.compareTo(this.montantCPDisponible) > 0) {
            throw new BusinessException(ErrorCode.MONTANT_CP_INSUFFISANT);
        }
        this.montantCPConsomme = this.montantCPConsomme.add(montant);
        this.montantCPDisponible = this.montantCPDisponible.subtract(montant);
    }

    @Override
    public void libererCP(BigDecimal montant) {
        this.montantCPConsomme = this.montantCPConsomme.subtract(montant);
        this.montantCPDisponible = this.montantCPDisponible.add(montant);
    }

    // ── StatutTransitionable ──────────────────────────

    // DISPONIBLE → ENGAGE
    @Override
    public void engager() {
        validerTransition(
                Set.of(StatutCredit.DISPONIBLE),
                StatutCredit.ENGAGE,
                "Seul un crédit DISPONIBLE peut être engagé"
        );
        this.statut = StatutCredit.ENGAGE;
    }

    // ENGAGE → SUSPENDU
    @Override
    public void suspendre() {
        validerTransition(
                Set.of(StatutCredit.ENGAGE),
                StatutCredit.SUSPENDU,
                "Seul un crédit ENGAGÉ peut être suspendu"
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

    // ENGAGE → SOLDE
    @Override
    public void solder() {
        validerTransition(
                Set.of(StatutCredit.ENGAGE),
                StatutCredit.SOLDE,
                "Seul un crédit ENGAGÉ peut être soldé"
        );
        this.statut = StatutCredit.SOLDE;
    }

    // DISPONIBLE → ANNULE
    // ENGAGE     → ANNULE
    // SUSPENDU   → ANNULE
    @Override
    public void annuler() {
        validerTransition(
                Set.of(StatutCredit.DISPONIBLE, StatutCredit.ENGAGE, StatutCredit.SUSPENDU),
                StatutCredit.ANNULE,
                "Un crédit SOLDÉ ne peut pas être annulé"
        );
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
        return this.statut == StatutCredit.SOLDE
                || this.statut == StatutCredit.ANNULE;
    }
}