package gov.cmr.minfi.db.gbe.app.identite.user.dto;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth
) {
}
