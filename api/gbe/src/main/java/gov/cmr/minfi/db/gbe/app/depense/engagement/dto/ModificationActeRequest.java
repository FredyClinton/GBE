package gov.cmr.minfi.db.gbe.app.depense.engagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Seuls l'objet et le délai se modifient : les montants, le fournisseur et les lignes viennent de l'offre retenue. */
public record ModificationActeRequest(
        @NotBlank String objet,
        @NotNull @Positive @Schema(description = "Délai de livraison en mois") Integer delaiLivraisonMois
) {
}
