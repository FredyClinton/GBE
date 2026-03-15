package gov.cmr.minfi.db.gbe.app.auth;

import gov.cmr.minfi.db.gbe.app.auth.request.AuthenticationRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.RefreshRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.RegistrationRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.VerificationRequest;
import gov.cmr.minfi.db.gbe.app.auth.response.AuthenticationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody
            AuthenticationRequest request) {
        return ResponseEntity.ok(this.authenticationService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody
            RegistrationRequest request) {

        var response = this.authenticationService.register(request);
        if (request.mfaEnabled()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid
            @RequestBody
            RefreshRequest request) {
        return ResponseEntity.ok(this.authenticationService.refreshToken(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @RequestBody VerificationRequest verificationRequest
    ) {
        return ResponseEntity.ok(this.authenticationService.verifyCode(verificationRequest));

    }

}
