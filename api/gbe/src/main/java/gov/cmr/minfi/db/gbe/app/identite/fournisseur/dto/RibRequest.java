package gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RibRequest(
        @NotBlank
        @Schema(description = "RIB à 23 caractères (espaces et tirets acceptés)", example = "10005 00001 01234567890 12")
        String rib
) {}
