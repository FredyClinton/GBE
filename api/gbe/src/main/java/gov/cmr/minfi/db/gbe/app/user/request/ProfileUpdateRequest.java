package gov.cmr.minfi.db.gbe.app.user.request;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth
) {
}
