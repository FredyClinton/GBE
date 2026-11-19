package gov.cmr.minfi.db.gbe.app.identite.user;

import gov.cmr.minfi.db.gbe.app.identite.auth.dto.UserContext;
import gov.cmr.minfi.db.gbe.app.identite.user.dto.ChangePasswordRequest;
import gov.cmr.minfi.db.gbe.app.identite.user.dto.ProfileUpdateRequest;

public interface UserService {
    /** Profil et mandats valides de l'utilisateur, relus en base (même contenu que userContext à la connexion). */
    UserContext getProfil(String userId);

    void updateProfileInfo(ProfileUpdateRequest request, String userId);

    void changePassword(ChangePasswordRequest request, String userId);

    void deactivateAccount(String userId);

    void reactivateAccount(String userId);

    void deleteAccount(String userId);
}
