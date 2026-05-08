package gov.cmr.minfi.db.gbe.app.auth.impl;

import gov.cmr.minfi.db.gbe.app.auth.AuthMapper;
import gov.cmr.minfi.db.gbe.app.auth.AuthenticationService;
import gov.cmr.minfi.db.gbe.app.auth.dto.request.*;
import gov.cmr.minfi.db.gbe.app.auth.dto.response.AuthenticationResponse;
import gov.cmr.minfi.db.gbe.app.auth.dto.response.MandatContext;
import gov.cmr.minfi.db.gbe.app.auth.dto.response.UserContext;
import gov.cmr.minfi.db.gbe.app.auth.tfa.TwoFactorAuthenticationService;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.mandat.Mandat;
import gov.cmr.minfi.db.gbe.app.mandat.MandatMapper;
import gov.cmr.minfi.db.gbe.app.mandat.MandatRepository;
import gov.cmr.minfi.db.gbe.app.security.JwtService;
import gov.cmr.minfi.db.gbe.app.user.User;
import gov.cmr.minfi.db.gbe.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	private final AuthenticationManager authenticationManager;
	private final TwoFactorAuthenticationService tfaService;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final MandatRepository mandatRepository;
	private final MandatMapper mandatMapper;
	private final AuthMapper authMapper;

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
		return AuthenticationResponse
			.builder()
			.accessToken(jwtService.refreshAccessToken(request.refreshToken()))
			.refreshToken(request.refreshToken())
			.tokenType("Bearer")
			.build();
	}

	// ── Helpers privés ────────────────────────────────

	private AuthenticationResponse buildResponse(User user) {
		final List<Mandat> mandats = mandatRepository.findMandatsValidesParUser(user.getId());

		return AuthenticationResponse
			.builder()
			.accessToken(jwtService.generateAccessToken(user.getUsername()))
			.refreshToken(jwtService.generateRefreshToken(user.getUsername()))
			.tokenType("Bearer")
			.firstLogin(false)
			.mfaEnabled(true)
			.userContext(buildUserContext(user, mandats))
			.build();
	}

	private UserContext buildUserContext(User user, List<Mandat> mandats) {
		final List<MandatContext> mandatContexts = mandatMapper.toContextList(mandats);
		return authMapper.toUserContext(user, mandatContexts);
	}
}
