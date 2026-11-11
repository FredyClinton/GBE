package gov.cmr.minfi.db.gbe.app.depense.engagement;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Réservation des crédits (UC07) : les AE de la ligne sont « isolées et déduites du montant disponible »
 * ({@link CreditBudgetaire#consommerAE}) pour le TTC de l'acte (Q17). Créée seulement par un visa favorable (Q1, Q2).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "reservations_credit")
public class ReservationCredit extends BaseEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @Column(name = "montant", nullable = false, precision = 20, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_reservation", nullable = false)
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private StatutReservation statut = StatutReservation.ACTIVE;

    @Column(name = "date_annulation")
    private LocalDateTime dateAnnulation;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acte_juridique_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_reservation_acte"))
    private ActeJuridique acteJuridique;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_budgetaire_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_credit"))
    private CreditBudgetaire creditBudgetaire;

    /** bloquer() de l'onglet 4 : consomme les AE sur la ligne (refusé si la ligne est bloquée ou insuffisante). */
    public static ReservationCredit bloquer(String numero, ActeJuridique acte, CreditBudgetaire credit,
                                            LocalDateTime maintenant) {
        credit.consommerAE(acte.getMontantTTC());
        return ReservationCredit.builder()
                .numero(numero)
                .montant(acte.getMontantTTC())
                .dateReservation(maintenant)
                .acteJuridique(acte)
                .creditBudgetaire(credit)
                .build();
    }
}
