package gov.cmr.minfi.db.gbe.app.identite.auth;

import gov.cmr.minfi.db.gbe.app.identite.auth.dto.AuthenticationRequest;
import gov.cmr.minfi.db.gbe.app.identite.auth.dto.RefreshRequest;
import gov.cmr.minfi.db.gbe.app.identite.auth.dto.SetupMfaRequest;
import gov.cmr.minfi.db.gbe.app.identite.auth.dto.VerificationRequest;
import gov.cmr.minfi.db.gbe.app.identite.auth.dto.AuthenticationResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.AUTHENTIFICATION)
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(
        summary = "Se connecter",
        description = "Route publique : vérifie l'e-mail et le mot de passe. " +
            "Si la double authentification est active, retourne un jeton MFA de courte durée à confirmer par /verify, ou par /setup-mfa lors de sa première configuration ; sinon retourne les jetons d'accès et de rafraîchissement. " +
            "Le champ firstLogin impose un changement de mot de passe."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentification réussie ou jeton MFA retourné"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Mot de passe incorrect ou compte désactivé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @Operation(
        summary = "Configurer la double authentification",
        description = "Route publique : confirme le secret TOTP avec un code de l'application d'authentification, puis retourne les jetons."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Double authentification configurée"),
        @ApiResponse(responseCode = "400", description = "Double authentification déjà confirmée (MFA_ALREADY_CONFIRMED)"),
        @ApiResponse(responseCode = "401", description = "Jeton MFA invalide ou expiré, ou code TOTP incorrect"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PostMapping("/setup-mfa")
    public ResponseEntity<AuthenticationResponse> setupMfa(
            @Valid @RequestBody SetupMfaRequest request
    ) {
        return ResponseEntity.ok(authenticationService.setupMfa(request));
    }

    @Operation(
        summary = "Vérifier le code TOTP",
        description = "Route publique : échange le jeton MFA et un code TOTP valide contre les jetons d'accès et de rafraîchissement."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Code vérifié, jetons retournés"),
        @ApiResponse(responseCode = "401", description = "Jeton MFA invalide ou expiré, ou code TOTP incorrect"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PostMapping("/verify")
    public ResponseEntity<AuthenticationResponse> verify(
            @Valid @RequestBody VerificationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.verifyCode(request));
    }

    @Operation(
        summary = "Renouveler les jetons",
        description = "Route publique : échange un jeton de rafraîchissement valide contre un nouveau jeton d'accès. " +
            "Le jeton de rafraîchissement est retourné inchangé et reste valable jusqu'à son expiration. " +
            "Le renouvellement est refusé si le jeton est mal formé, expiré, d'un autre type (accès ou MFA), " +
            "ou si le compte a été supprimé, désactivé ou verrouillé depuis la connexion."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nouveau jeton d'accès retourné"),
        @ApiResponse(responseCode = "400", description = "Jeton de rafraîchissement absent"),
        @ApiResponse(responseCode = "401", description = "Jeton de rafraîchissement invalide ou expiré, ou compte inactif (REFRESH_TOKEN_INVALIDE)")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }
}
