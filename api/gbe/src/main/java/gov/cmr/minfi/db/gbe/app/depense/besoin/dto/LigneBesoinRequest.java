package gov.cmr.minfi.db.gbe.app.depense.besoin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LigneBesoinRequest(
        @NotBlank String matiereId,
        @NotNull @Positive BigDecimal quantiteDemandee,
        @Schema(description = "Champ libre : caractéristiques techniques, unité, conditionnement…")
        String specifications
) {}
