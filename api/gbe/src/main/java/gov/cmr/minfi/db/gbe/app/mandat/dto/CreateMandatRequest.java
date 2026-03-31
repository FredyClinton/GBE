package gov.cmr.minfi.db.gbe.app.mandat.dto;

import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateMandatRequest(
        @NotBlank(message = "VALIDATION.MANDAT.SECTION.NOT_BLANK")
        @Schema(example = "uuid-de-la-section")
        String sectionId,

        // Nullable — null = scope section entière (MINISTRE)
        @Schema(example = "uuid-du-programme")
        String programmeId,

        @NotNull(message = "VALIDATION.MANDAT.ROLE.NOT_NULL")
        RoleSysteme roleSysteme,

        LocalDate dateDebut,

        LocalDate dateFin,

        @Schema(example = "ARRETE-2026-001-MINFI")
        String numeroDecision

) {
}
