package gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme;

import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.TypeActe;
import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Seuil d'une forme d'acte sur l'échelle BCA < LC < Marché (Q3). Le plafond TTC est exclusif ; {@code null} : pas de
 * plafond (dernière forme de l'échelle).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "seuils_forme_acte")
public class SeuilFormeActe extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type_acte", nullable = false, unique = true, length = 30)
    private TypeActe typeActe;

    /** Position sur l'échelle : 1 pour la forme la plus légère. */
    @Column(name = "rang", nullable = false, unique = true)
    private Integer rang;

    @Column(name = "plafond_ttc", precision = 20, scale = 2)
    private BigDecimal plafondTTC;

    /** Le montant TTC reste sous le plafond de cette forme. */
    public boolean couvre(BigDecimal montantTTC) {
        return plafondTTC == null || montantTTC.compareTo(plafondTTC) < 0;
    }
}
