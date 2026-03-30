package gov.cmr.minfi.db.gbe.app.agent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentResponse(
        String id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String matricule,
        String nui,
        String numeroCni,
        LocalDate cniIssueDate,
        LocalDate cniExpiryDate,
        String phoneNumber,
        boolean actif,
        String userId,
        String email

) {
}
