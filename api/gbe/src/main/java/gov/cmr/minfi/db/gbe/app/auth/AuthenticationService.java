package gov.cmr.minfi.db.gbe.app.auth;

import gov.cmr.minfi.db.gbe.app.auth.request.AuthenticationRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.RefreshRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.RegistrationRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.VerificationRequest;
import gov.cmr.minfi.db.gbe.app.auth.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse verifyCode(VerificationRequest verificationRequest);

    AuthenticationResponse login(AuthenticationRequest request);

    AuthenticationResponse register(RegistrationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest request);
}
