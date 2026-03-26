package gov.cmr.minfi.db.gbe.app.auth.impl;

import gov.cmr.minfi.db.gbe.app.affectation.UserAffectation;
import gov.cmr.minfi.db.gbe.app.affectation.UserAffectationRepository;
import gov.cmr.minfi.db.gbe.app.auth.dto.request.AuthenticationRequest;
import gov.cmr.minfi.db.gbe.app.auth.dto.request.RefreshRequest;
import gov.cmr.minfi.db.gbe.app.auth.dto.request.SetupMfaRequest;
import gov.cmr.minfi.db.gbe.app.auth.dto.request.VerificationRequest;
import gov.cmr.minfi.db.gbe.app.auth.dto.response.AuthenticationResponse;
import gov.cmr.minfi.db.gbe.app.auth.tfa.TwoFactorAuthenticationService;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.iam.role.Role;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.security.JwtService;
import gov.cmr.minfi.db.gbe.app.user.User;
import gov.cmr.minfi.db.gbe.app.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl — Tests unitaires")
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TwoFactorAuthenticationService tfaService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserAffectationRepository userAffectationRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    // ================================================
    // Fixtures communes
    // ================================================
    private User userPremierConnexion;
    private User userConnexionSuivante;
    private Role role;
    private Section section;
    private Programme programme;
    private UserAffectation affectation;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName("ROLE_ORDONNATEUR_PRINCIPAL");

        section = Section.builder()
                .codeSection("20")
                .libelleFr("Ministère des Finances")
                .libelleEn("Ministry of Finance")
                .sigle("MINFI")
                .build();

        programme = Programme.builder()
                .code("001")
                .libelleFr("Pilotage et coordination")
                .section(section)
                .build();

        userPremierConnexion = User.builder()
                .email("jean.dupont@minfi.cm")
                .password("encoded_password")
                .firstName("Jean")
                .lastName("Dupont")
                .firstLogin(true)
                .mfaEnabled(false)
                .secret("TOTP_SECRET")
                .role(role)
                .build();

        userConnexionSuivante = User.builder()
                .email("admin@minfi.cm")
                .password("encoded_password")
                .firstName("Super")
                .lastName("Admin")
                .firstLogin(false)
                .mfaEnabled(true)
                .secret("TOTP_SECRET")
                .role(role)
                .build();

        affectation = UserAffectation.builder()
                .user(userConnexionSuivante)
                .section(section)
                .programme(programme)
                .roleSysteme(RoleSysteme.ORDONNATEUR_PRINCIPAL)
                .actif(true)
                .build();
        affectation.initialiserPermissionsDepuisRole();
    }

    // ================================================
    // Tests — login()
    // ================================================
    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Première connexion → retourne mfaToken + secretImageUri")
        void login_premiereConnexion_retourneQrCodeEtMfaToken() {
            // GIVEN
            final Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(userPremierConnexion);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtService.generateMfaToken(anyString())).thenReturn("mfa-token-123");
            when(tfaService.generateQrCodeImageUri(anyString())).thenReturn("data:image/png;base64,abc");

            final AuthenticationRequest request = new AuthenticationRequest(
                    "jean.dupont@minfi.cm", "Test@1234"
            );

            // WHEN
            final AuthenticationResponse response = authenticationService.login(request);

            // THEN
            assertThat(response.firstLogin()).isTrue();
            assertThat(response.mfaEnabled()).isFalse();
            assertThat(response.mfaToken()).isEqualTo("mfa-token-123");
            assertThat(response.secretImageUri()).isEqualTo("data:image/png;base64,abc");
            assertThat(response.accessToken()).isNull();
            assertThat(response.refreshToken()).isNull();

            verify(jwtService).generateMfaToken("jean.dupont@minfi.cm");
            verify(tfaService).generateQrCodeImageUri("TOTP_SECRET");
        }

        @Test
        @DisplayName("Connexions suivantes → retourne mfaToken uniquement")
        void login_connexionSuivante_retourneMfaTokenUniquement() {
            // GIVEN
            final Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(userConnexionSuivante);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtService.generateMfaToken(anyString())).thenReturn("mfa-token-456");

            final AuthenticationRequest request = new AuthenticationRequest(
                    "admin@minfi.cm", "Admin@1234"
            );

            // WHEN
            final AuthenticationResponse response = authenticationService.login(request);

            // THEN
            assertThat(response.firstLogin()).isFalse();
            assertThat(response.mfaEnabled()).isTrue();
            assertThat(response.mfaToken()).isEqualTo("mfa-token-456");
            assertThat(response.secretImageUri()).isNull();
            assertThat(response.accessToken()).isNull();

            verify(tfaService, never()).generateQrCodeImageUri(anyString());
        }

        @Test
        @DisplayName("Credentials invalides → lève BadCredentialsException")
        void login_credentialsInvalides_leveBadCredentialsException() {
            // GIVEN
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            final AuthenticationRequest request = new AuthenticationRequest(
                    "jean.dupont@minfi.cm", "mauvais_mdp"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            verify(jwtService, never()).generateMfaToken(anyString());
        }

        @Test
        @DisplayName("Login → génère toujours un mfaToken après credentials valides")
        void login_credentialsValides_genereToujursMfaToken() {
            // GIVEN
            final Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(userConnexionSuivante);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtService.generateMfaToken(anyString())).thenReturn("mfa-token");

            final AuthenticationRequest request = new AuthenticationRequest(
                    "admin@minfi.cm", "Admin@1234"
            );

            // WHEN
            authenticationService.login(request);

            // THEN — vérifier que le mfaToken est toujours généré après les credentials
            verify(jwtService, times(1)).generateMfaToken("admin@minfi.cm");
        }
    }

    // ================================================
    // Tests — verifyCode()
    // ================================================
    @Nested
    @DisplayName("verifyCode()")
    class VerifyCodeTests {

        @Test
        @DisplayName("Code valide + mfaToken valide → retourne les JWT")
        void verifyCode_codeEtTokenValides_retourneJWT() {
            // GIVEN
            when(jwtService.extractUsernameFromMfaToken("mfa-token"))
                    .thenReturn("admin@minfi.cm");
            when(userRepository.findByEmailIgnoreCase("admin@minfi.cm"))
                    .thenReturn(Optional.of(userConnexionSuivante));
            when(tfaService.isNonOtpValid("TOTP_SECRET", "123456")).thenReturn(false);
            when(jwtService.generateAccessToken(anyString())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(userAffectationRepository.findByUserIdAndActifTrue(any()))
                    .thenReturn(List.of(affectation));

            final VerificationRequest request = new VerificationRequest(
                    "admin@minfi.cm", "123456", "mfa-token"
            );

            // WHEN
            final AuthenticationResponse response = authenticationService.verifyCode(request);

            // THEN
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.userContext()).isNotNull();
            assertThat(response.userContext().email()).isEqualTo("admin@minfi.cm");
        }

        @Test
        @DisplayName("mfaToken invalide → lève INVALID_MFA_TOKEN")
        void verifyCode_mfaTokenInvalide_leveInvalidMfaToken() {
            // GIVEN
            when(jwtService.extractUsernameFromMfaToken("token-invalide"))
                    .thenThrow(new RuntimeException("Invalid token"));

            final VerificationRequest request = new VerificationRequest(
                    "admin@minfi.cm", "123456", "token-invalide"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.verifyCode(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        final BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_MFA_TOKEN);
                    });

            verify(userRepository, never()).findByEmailIgnoreCase(anyString());
        }

        @Test
        @DisplayName("Email ne correspond pas au mfaToken → lève INVALID_MFA_TOKEN")
        void verifyCode_emailDifferentDuToken_leveInvalidMfaToken() {
            // GIVEN — le token appartient à un autre utilisateur
            when(jwtService.extractUsernameFromMfaToken("mfa-token"))
                    .thenReturn("autre.utilisateur@minfi.cm");

            final VerificationRequest request = new VerificationRequest(
                    "admin@minfi.cm", "123456", "mfa-token"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.verifyCode(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_MFA_TOKEN));

            verify(userRepository, never()).findByEmailIgnoreCase(anyString());
        }

        @Test
        @DisplayName("Code TOTP invalide → lève BAD_CREDENTIALS")
        void verifyCode_codeTotpInvalide_leveBadCredentials() {
            // GIVEN
            when(jwtService.extractUsernameFromMfaToken("mfa-token"))
                    .thenReturn("admin@minfi.cm");
            when(userRepository.findByEmailIgnoreCase("admin@minfi.cm"))
                    .thenReturn(Optional.of(userConnexionSuivante));
            when(tfaService.isNonOtpValid("TOTP_SECRET", "000000")).thenReturn(true);

            final VerificationRequest request = new VerificationRequest(
                    "admin@minfi.cm", "000000", "mfa-token"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.verifyCode(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.BAD_CREDENTIALS));
        }

        @Test
        @DisplayName("Utilisateur introuvable → lève USER_NOT_FOUND")
        void verifyCode_utilisateurIntrouvable_leveUserNotFound() {
            // GIVEN
            when(jwtService.extractUsernameFromMfaToken("mfa-token"))
                    .thenReturn("inconnu@minfi.cm");
            when(userRepository.findByEmailIgnoreCase("inconnu@minfi.cm"))
                    .thenReturn(Optional.empty());

            final VerificationRequest request = new VerificationRequest(
                    "inconnu@minfi.cm", "123456", "mfa-token"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.verifyCode(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }

        @Test
        @DisplayName("Faille sécurité corrigée — sans mfaToken on ne peut pas accéder à verify")
        void verifyCode_sansPasserParLogin_estImpossible() {
            // GIVEN — simuler un token invalide (pas généré par /login)
            when(jwtService.extractUsernameFromMfaToken("token-forge"))
                    .thenThrow(new RuntimeException("Invalid JWT"));

            final VerificationRequest request = new VerificationRequest(
                    "admin@minfi.cm", "123456", "token-forge"
            );

            // WHEN / THEN — la faille est corrigée
            assertThatThrownBy(() -> authenticationService.verifyCode(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_MFA_TOKEN));

            // Vérifier qu'on n'a jamais cherché l'utilisateur en base
            verify(userRepository, never()).findByEmailIgnoreCase(anyString());
        }
    }

    // ================================================
    // Tests — setupMfa()
    // ================================================
    @Nested
    @DisplayName("setupMfa()")
    class SetupMfaTests {

        @Test
        @DisplayName("Première connexion valide → active MFA et retourne les JWT")
        void setupMfa_premiereConnexionValide_activeMfaEtRetourneJWT() {
            // GIVEN
            when(jwtService.extractUsernameFromMfaToken("mfa-token"))
                    .thenReturn("jean.dupont@minfi.cm");
            when(userRepository.findByEmailIgnoreCase("jean.dupont@minfi.cm"))
                    .thenReturn(Optional.of(userPremierConnexion));
            when(tfaService.isNonOtpValid("TOTP_SECRET", "123456")).thenReturn(false);
            when(jwtService.generateAccessToken(anyString())).thenReturn("access-token");
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");
            when(userAffectationRepository.findByUserIdAndActifTrue(any()))
                    .thenReturn(List.of());

            final SetupMfaRequest request = new SetupMfaRequest(
                    "jean.dupont@minfi.cm", "123456", "mfa-token"
            );

            // WHEN
            final AuthenticationResponse response = authenticationService.setupMfa(request);

            // THEN
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");

            // Vérifier que l'utilisateur est modifié en base
            assertThat(userPremierConnexion.isMfaEnabled()).isTrue();
            assertThat(userPremierConnexion.isFirstLogin()).isFalse();
            verify(userRepository).save(userPremierConnexion);
        }

        @Test
        @DisplayName("MFA déjà confirmé → lève MFA_ALREADY_CONFIRMED")
        void setupMfa_mfaDejaConfirme_leveMfaAlreadyConfirmed() {
            // GIVEN — utilisateur dont le MFA est déjà actif
            when(jwtService.extractUsernameFromMfaToken("mfa-token"))
                    .thenReturn("admin@minfi.cm");
            when(userRepository.findByEmailIgnoreCase("admin@minfi.cm"))
                    .thenReturn(Optional.of(userConnexionSuivante));

            final SetupMfaRequest request = new SetupMfaRequest(
                    "admin@minfi.cm", "123456", "mfa-token"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.setupMfa(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.MFA_ALREADY_CONFIRMED));

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("mfaToken invalide → lève INVALID_MFA_TOKEN sans toucher à la base")
        void setupMfa_mfaTokenInvalide_leveInvalidMfaToken() {
            // GIVEN
            when(jwtService.extractUsernameFromMfaToken("token-invalide"))
                    .thenThrow(new RuntimeException("Invalid token"));

            final SetupMfaRequest request = new SetupMfaRequest(
                    "jean.dupont@minfi.cm", "123456", "token-invalide"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.setupMfa(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_MFA_TOKEN));

            verify(userRepository, never()).findByEmailIgnoreCase(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Code TOTP invalide → lève BAD_CREDENTIALS sans modifier l'utilisateur")
        void setupMfa_codeTotpInvalide_leveBadCredentialsSansModifier() {
            // GIVEN
            when(jwtService.extractUsernameFromMfaToken("mfa-token"))
                    .thenReturn("jean.dupont@minfi.cm");
            when(userRepository.findByEmailIgnoreCase("jean.dupont@minfi.cm"))
                    .thenReturn(Optional.of(userPremierConnexion));
            when(tfaService.isNonOtpValid("TOTP_SECRET", "000000")).thenReturn(true);

            final SetupMfaRequest request = new SetupMfaRequest(
                    "jean.dupont@minfi.cm", "000000", "mfa-token"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.setupMfa(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.BAD_CREDENTIALS));

            // L'utilisateur ne doit pas être modifié
            assertThat(userPremierConnexion.isMfaEnabled()).isFalse();
            assertThat(userPremierConnexion.isFirstLogin()).isTrue();
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Email ne correspond pas au mfaToken → lève INVALID_MFA_TOKEN")
        void setupMfa_emailDifferentDuToken_leveInvalidMfaToken() {
            // GIVEN
            when(jwtService.extractUsernameFromMfaToken("mfa-token"))
                    .thenReturn("autre@minfi.cm");

            final SetupMfaRequest request = new SetupMfaRequest(
                    "jean.dupont@minfi.cm", "123456", "mfa-token"
            );

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.setupMfa(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_MFA_TOKEN));
        }
    }

    // ================================================
    // Tests — refreshToken()
    // ================================================
    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTests {

        @Test
        @DisplayName("Refresh token valide → retourne un nouvel access token")
        void refreshToken_tokenValide_retourneNouvelAccessToken() {
            // GIVEN
            when(jwtService.refreshAccessToken("refresh-token-valide"))
                    .thenReturn("nouveau-access-token");

            final RefreshRequest request = new RefreshRequest("refresh-token-valide");

            // WHEN
            final AuthenticationResponse response = authenticationService.refreshToken(request);

            // THEN
            assertThat(response.accessToken()).isEqualTo("nouveau-access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token-valide");
            assertThat(response.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Refresh token invalide → lève RuntimeException")
        void refreshToken_tokenInvalide_leveRuntimeException() {
            // GIVEN
            when(jwtService.refreshAccessToken("token-invalide"))
                    .thenThrow(new RuntimeException("Refresh Token expired"));

            final RefreshRequest request = new RefreshRequest("token-invalide");

            // WHEN / THEN
            assertThatThrownBy(() -> authenticationService.refreshToken(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Refresh Token expired");
        }
    }
}