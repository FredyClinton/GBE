package gov.cmr.minfi.db.gbe.app.depense.soumission;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.depense.besoin.LigneBesoin;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/** Ligne d'une soumission (onglet 4) : prix unitaire proposé pour une ligne du besoin. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "lignes_soumission")
public class LigneSoumission extends BaseEntity {

    /** Repris de la ligne de besoin. */
    @Column(name = "numero_ordre", nullable = false)
    private Integer numeroOrdre;

    @Column(name = "prix_unitaire_propose", nullable = false, precision = 20, scale = 2)
    private BigDecimal prixUnitairePropose;

    /** prixUnitairePropose × quantité demandée (calculerSousTotalHT du modèle). */
    @Column(name = "montant_total_ligne_ht", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantTotalLigneHT;

    @Column(name = "remarques_techniques", columnDefinition = "TEXT")
    private String remarquesTechniques;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ligne_besoin_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ligne_soumission_ligne_besoin"))
    private LigneBesoin ligneBesoin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soumission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ligne_soumission_soumission"))
    private Soumission soumission;
}
