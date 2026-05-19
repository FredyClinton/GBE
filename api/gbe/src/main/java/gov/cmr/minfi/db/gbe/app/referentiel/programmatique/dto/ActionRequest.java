package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto;

import jakarta.validation.constraints.NotBlank;

public record ActionRequest(
        @NotBlank String codeAction,
        @NotBlank String libelleFr,
        String libelleEn,
        String numero,
        @NotBlank String programmeId,
        @NotBlank String sectionId,
        @NotBlank String exerciceId
) {}
