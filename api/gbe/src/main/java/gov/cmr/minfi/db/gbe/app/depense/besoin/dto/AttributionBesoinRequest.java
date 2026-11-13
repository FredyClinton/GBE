package gov.cmr.minfi.db.gbe.app.depense.besoin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Décision Q13 : l'ordonnateur sélectionne seulement l'offre retenue par la commission. */
public record AttributionBesoinRequest(
        @NotBlank @Schema(description = "Identifiant de la soumission retenue, déposée sur ce besoin") String soumissionId) {
}
