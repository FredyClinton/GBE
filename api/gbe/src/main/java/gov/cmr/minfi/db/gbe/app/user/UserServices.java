package gov.cmr.minfi.db.gbe.app.user;

import gov.cmr.minfi.db.gbe.app.user.request.ChangePasswordRequest;
import gov.cmr.minfi.db.gbe.app.user.request.ProfileUpdateRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserServices extends UserDetailsService {
    void updateProfileInfo(ProfileUpdateRequest request, String userId);
    void changePassword(ChangePasswordRequest request, String userId);
    void deactivateAccount(String userId);
    void reactivateAccount(String userId);
    void deleteAccount(String userId);
}
