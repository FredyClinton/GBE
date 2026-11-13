package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.Matiere;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/** Ligne d'une expression de besoin : matière, quantité et spécifications, sans aucun prix. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "lignes_besoin")
public class LigneBesoin extends BaseEntity {

    @Column(name = "numero_ordre", nullable = false)
    private Integer numeroOrdre;

    @Column(name = "quantite_demandee", nullable = false, precision = 18, scale = 3)
    private BigDecimal quantiteDemandee;

    /** Champ libre : caractéristiques techniques, unité, conditionnement… */
    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matiere_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ligne_besoin_matiere"))
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expression_besoin_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ligne_besoin_expression"))
    private ExpressionBesoin expressionBesoin;
}
