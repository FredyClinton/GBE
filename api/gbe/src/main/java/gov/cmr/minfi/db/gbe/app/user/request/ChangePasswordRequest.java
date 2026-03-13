package gov.cmr.minfi.db.gbe.app.user.request;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {
}
