package gov.cmr.minfi.db.gbe.app.identite.user.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {
}
