package gov.cmr.minfi.db.gbe.app.depense.engagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Seuls l'objet et les données de la forme (délai, devis) se modifient : les montants, le fournisseur et les lignes viennent de l'offre retenue. */
public record ModificationActeRequest(
        @NotBlank String objet,
        @NotNull @Positive @Schema(description = "BCA : délai de livraison en mois") Integer delaiLivraisonMois,
        @Size(max = 100) @Schema(description = "BCA : référence du devis du fournisseur, facultative (remplacée)") String referenceDevis
) {
}
