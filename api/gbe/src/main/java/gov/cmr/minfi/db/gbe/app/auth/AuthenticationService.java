package gov.cmr.minfi.db.gbe.app.auth;

import gov.cmr.minfi.db.gbe.app.auth.request.AuthenticationRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.RefreshRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.RegistrationRequest;
import gov.cmr.minfi.db.gbe.app.auth.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse login(AuthenticationRequest request);

    void register(RegistrationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest request);
}
