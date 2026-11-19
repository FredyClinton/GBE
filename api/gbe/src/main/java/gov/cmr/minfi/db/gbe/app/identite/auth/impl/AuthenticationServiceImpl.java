package gov.cmr.minfi.db.gbe.app.identite.auth.impl;

import gov.cmr.minfi.db.gbe.app.identite.auth.AuthenticationService;
import gov.cmr.minfi.db.gbe.app.identite.auth.dto.*;
import gov.cmr.minfi.db.gbe.app.identite.auth.tfa.TwoFactorAuthenticationService;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.security.JwtService;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import gov.cmr.minfi.db.gbe.app.identite.user.UserRepository;
import gov.cmr.minfi.db.gbe.app.identite.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	private final AuthenticationManager authenticationManager;
	private final TwoFactorAuthenticationService tfaService;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final UserService userService;

	@Override
	public AuthenticationResponse login(AuthenticationRequest request) {
		final Authentication auth = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);

		final User user = (User) auth.getPrincipal();
		if (user == null) {
			throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, request.email());
		}

		final String mfaToken = jwtService.generateMfaToken(user.getUsername());

		if (user.isFirstLogin()) {
			if (user.getSecret() == null) {
				user.setSecret(tfaService.generateNewSecret());
				userRepository.save(user);
			}
			return AuthenticationResponse
				.builder()
				.firstLogin(true)
				.mfaEnabled(false)
				.secretImageUri(tfaService.generateQrCodeImageUri(user.getSecret()))
				.mfaToken(mfaToken)
				.build();
		}

		return AuthenticationResponse.builder().firstLogin(false).mfaEnabled(true).mfaToken(mfaToken).build();
	}

	@Override
	public AuthenticationResponse verifyCode(VerificationRequest request) {
		final String usernameFromToken;
		try {
			usernameFromToken = jwtService.extractUsernameFromMfaToken(request.mfaToken());
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.INVALID_MFA_TOKEN);
		}

		if (!usernameFromToken.equalsIgnoreCase(request.email())) {
			throw new BusinessException(ErrorCode.INVALID_MFA_TOKEN);
		}

		final User user = userRepository
			.findByEmailIgnoreCase(request.email())
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, request.email()));

		if (tfaService.isNonOtpValid(user.getSecret(), request.code())) {
			throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
		}

		return buildResponse(user);
	}

	@Override
	@Transactional
	public AuthenticationResponse setupMfa(SetupMfaRequest request) {
		final String usernameFromToken;
		try {
			usernameFromToken = jwtService.extractUsernameFromMfaToken(request.mfaToken());
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.INVALID_MFA_TOKEN);
		}

		if (!usernameFromToken.equalsIgnoreCase(request.email())) {
			throw new BusinessException(ErrorCode.INVALID_MFA_TOKEN);
		}

		final User user = userRepository
			.findByEmailIgnoreCase(request.email())
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, request.email()));

		if (!user.isFirstLogin()) {
			throw new BusinessException(ErrorCode.MFA_ALREADY_CONFIRMED);
		}

		if (tfaService.isNonOtpValid(user.getSecret(), request.code())) {
			throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
		}

		user.setMfaEnabled(true);
		user.setFirstLogin(false);
		userRepository.save(user);
		log.info("MFA activé pour : {}", user.getEmail());

		return buildResponse(user);
	}

	@Override
	public AuthenticationResponse register(RegistrationRequest request) {
		return null;
	}

	@Override
	public AuthenticationResponse refreshToken(RefreshRequest request) {
		final String email = jwtService.extraireSujetRefreshToken(request.refreshToken());
		// Un compte supprimé, désactivé ou verrouillé depuis l'émission du jeton ne peut plus le renouveler.
		final User user = userRepository.findByEmailIgnoreCase(email)
			.filter(u -> u.isEnabled() && u.isAccountNonLocked())
			.orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALIDE));
		return AuthenticationResponse
			.builder()
			.accessToken(jwtService.generateAccessToken(user.getUsername()))
			.refreshToken(request.refreshToken())
			.tokenType("Bearer")
			.build();
	}

	// ── Helpers privés ────────────────────────────────

	private AuthenticationResponse buildResponse(User user) {
		return AuthenticationResponse
			.builder()
			.accessToken(jwtService.generateAccessToken(user.getUsername()))
			.refreshToken(jwtService.generateRefreshToken(user.getUsername()))
			.tokenType("Bearer")
			.firstLogin(false)
			.mfaEnabled(true)
			// même contenu que GET /api/v1/users/me
			.userContext(userService.getProfil(user.getId()))
			.build();
	}
}
