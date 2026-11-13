package gov.cmr.minfi.db.gbe.app.depense.controle;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.depense.engagement.ActeJuridique;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Visa du CF (classe Visa de l'onglet 4). Le quitus sur le classement en nature économique (UC08) est porté par
 * le visa favorable ({@link #referenceQuitus}, décision Q19). Le CF auteur est tracé par {@code createdBy}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "visas")
public class Visa extends BaseEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_visa", nullable = false, length = 30)
    private TypeVisa typeVisa;

    /** Date à laquelle le dossier a été transmis au CF. */
    @Column(name = "date_reception", nullable = false)
    private LocalDateTime dateReception;

    @Column(name = "date_visa", nullable = false)
    private LocalDateTime dateVisa;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 30)
    private StatutVisa decision;

    /** Motif du rejet ou des réserves (obligatoire dans ces deux cas), remarques sinon. */
    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "reference_quitus", length = 30)
    private String referenceQuitus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acte_juridique_id", nullable = false, foreignKey = @ForeignKey(name = "fk_visa_acte"))
    private ActeJuridique acteJuridique;

    public boolean estFavorable() {
        return decision == StatutVisa.FAVORABLE;
    }
}
