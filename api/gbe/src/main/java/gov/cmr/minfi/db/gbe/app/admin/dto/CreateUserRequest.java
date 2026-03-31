package gov.cmr.minfi.db.gbe.app.admin.dto;

import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateUserRequest(

        // ── Lien vers l'Agent ─────────────────────────────
        // L'agent doit exister avant la création du User
        @NotBlank(message = "VALIDATION.USER.AGENT.NOT_BLANK")
        @Schema(description = "ID de l'agent auquel rattacher ce compte",
                example = "uuid-de-l-agent")
        String agentId,

        // ── Credentials ───────────────────────────────────
        @NotBlank(message = "VALIDATION.USER.EMAIL.NOT_BLANK")
        @Email(message = "VALIDATION.USER.EMAIL.FORMAT")
        @Schema(example = "mbarga.paul@minfi.cm")
        String email,

        @NotBlank(message = "VALIDATION.USER.PASSWORD.NOT_BLANK")
        @Size(min = 8, max = 72, message = "VALIDATION.USER.PASSWORD.SIZE")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$",
                message = "VALIDATION.USER.PASSWORD.WEAK"
        )
        @Schema(example = "Temp@1234")
        String password,

        // ── Rôle IAM ──────────────────────────────────────
        @NotNull(message = "VALIDATION.USER.ROLE.NOT_NULL")
        RoleSysteme roleSysteme,

        // ── Premier mandat ────────────────────────────────
        // La section est obligatoire
        @NotBlank(message = "VALIDATION.USER.SECTION.NOT_BLANK")
        @Schema(example = "uuid-de-la-section")
        String sectionId,

        // Les programmes sont optionnels — null ou liste vide = MINISTRE (scope section)
        @Schema(description = "Liste des IDs de programmes. "
                + "Vide ou absent = mandat section entière (MINISTRE)")
        List<String> programmeIds

) {
}