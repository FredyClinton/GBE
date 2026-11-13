package gov.cmr.minfi.db.gbe.app.identite.auth;

import gov.cmr.minfi.db.gbe.app.identite.auth.dto.*;
import gov.cmr.minfi.db.gbe.app.identite.auth.dto.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse verifyCode(VerificationRequest verificationRequest);

    AuthenticationResponse login(AuthenticationRequest request);

    AuthenticationResponse setupMfa(SetupMfaRequest request);

    AuthenticationResponse register(RegistrationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest request);
}
