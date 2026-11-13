package gov.cmr.minfi.db.gbe.app.referentiel.taxe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TauxTaxeRequest(
        @NotNull @DecimalMin("0") @DecimalMax(value = "100", inclusive = false)
        @Schema(description = "Taux en pourcentage, par exemple 19.25") BigDecimal taux) {
}
