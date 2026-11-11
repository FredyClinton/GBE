package gov.cmr.minfi.db.gbe.app.depense.engagement.forme;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.depense.engagement.ActeJuridique;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Données propres à la forme BCA (onglet 4 : StrategieBonDeCommande ; onglet 1 : BonDeCommandeAdministratif),
 * rattachées à l'acte par composition. Le comportement (numérotation, éligibilité, impression) est dans
 * {@link StrategieBonDeCommande}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "bons_de_commande")
public class BonDeCommande extends BaseEntity {

    /** Délai de livraison en mois, tel qu'imprimé sur le BCA (Q25). */
    @Column(name = "delai_livraison_mois", nullable = false)
    private Integer delaiLivraisonMois;

    @Column(name = "reference_devis", length = 100)
    private String referenceDevis;

    /** Rempli avec l'impression du BCA (lot 3, plan §7). */
    @Column(name = "code_barres_securite")
    private String codeBarresSecurite;

    /** Rempli avec l'impression du BCA (lot 3, plan §7). */
    @Column(name = "qr_code_securite", columnDefinition = "TEXT")
    private String qrCodeSecurite;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acte_juridique_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_bon_de_commande_acte"))
    private ActeJuridique acteJuridique;
}
