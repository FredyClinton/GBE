package gov.cmr.minfi.db.gbe.app.depense.controle.dto;

import gov.cmr.minfi.db.gbe.app.depense.controle.StatutVisa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record DecisionVisaRequest(
        @NotNull @Schema(description = "FAVORABLE, DEFAVORABLE_REJET ou SUSPENDU_RESERVES") StatutVisa decision,
        @Schema(description = "Motif du rejet ou des réserves (obligatoire dans ces deux cas), remarques sinon")
        String observations
) {
}
