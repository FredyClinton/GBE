package gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MatiereRequest(
        @NotBlank @Size(max = 100) String reference,
        @NotBlank String libelle
) {}
