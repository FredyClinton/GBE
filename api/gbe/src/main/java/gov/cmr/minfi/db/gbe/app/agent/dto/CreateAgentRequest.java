package gov.cmr.minfi.db.gbe.app.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAgentRequest(
        @Schema(example = "Mbarga")
        String firstName,

        @Schema(example = "Anicet")
        String lastName,

        @Schema(example = "AAAA-MM-DD")
        LocalDate dateOfBirth,
        @NotBlank(message = "VALIDATION.AGENT.MATRICULE.NOT_BLANK")
        @Size(min = 9, max = 9, message = "VALIDATION.AGENT.MATRICULE.SIZE")
        @Schema(example = "12345678N")
        String matricule,

        @NotBlank(message = "VALIDATION.AGENT.NUI.NOT_BLANK")
        @Schema(example = "NUI123456")
        String nui,

        @NotBlank(message = "VALIDATION.AGENT.CNI.NOT_BLANK")
        @Schema(example = "CN123456789")
        String numeroCni,

        @NotNull(message = "VALIDATION.AGENT.CNI_ISSUE_DATE.NOT_NULL")
        LocalDate cniIssueDate,

        @NotNull(message = "VALIDATION.AGENT.CNI_EXPIRY_DATE.NOT_NULL")
        LocalDate cniExpiryDate,

        @NotBlank(message = "VALIDATION.AGENT.PHONE.NOT_BLANK")
        @Pattern(regexp = "^\\+?[0-9]{9,13}", message = "VALIDATION.AGENT.PHONE.FORMAT")
        @Schema(example = "+237612345678")
        String phoneNumber
) {
}
