package gov.cmr.minfi.db.gbe.app.depense.soumission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LigneSoumissionRequest(
        @NotBlank String ligneBesoinId,
        @NotNull @Positive BigDecimal prixUnitairePropose,
        String remarquesTechniques
) {}
