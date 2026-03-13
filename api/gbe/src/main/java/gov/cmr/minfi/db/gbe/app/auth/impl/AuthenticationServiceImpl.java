package gov.cmr.minfi.db.gbe.app.auth.impl;

import gov.cmr.minfi.db.gbe.app.auth.AuthenticationService;
import gov.cmr.minfi.db.gbe.app.auth.request.AuthenticationRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.RefreshRequest;
import gov.cmr.minfi.db.gbe.app.auth.request.RegistrationRequest;
import gov.cmr.minfi.db.gbe.app.auth.response.AuthenticationResponse;
import gov.cmr.minfi.db.gbe.app.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.role.Role;
import gov.cmr.minfi.db.gbe.app.role.RoleRepository;
import gov.cmr.minfi.db.gbe.app.security.JwtService;
import gov.cmr.minfi.db.gbe.app.user.User;
import gov.cmr.minfi.db.gbe.app.user.UserMapper;
import gov.cmr.minfi.db.gbe.app.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;


    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        final Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        final User user = (User) auth.getPrincipal();
        final String accesToken = this.jwtService.generateAccesToken(user.getUsername());
        final String refrechToken = this.jwtService.generateRefreshToken(user.getUsername());
        final String tokenType = "Bearer";

        return new AuthenticationResponse(accesToken, refrechToken, tokenType);
    }

    @Override
    @Transactional
    public void register(RegistrationRequest request) {
        checkUserEmail(request.email());
        checkUserPhoneNumber(request.phoneNumber());
        checkPasswords(request.password(), request.confirmPassword());

        final Role userRole = this.roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new EntityNotFoundException("Role user does not exists"));

        final List<Role> roles = new ArrayList<>();
        roles.add(userRole);
        final User user = this.userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(roles);
        log.debug("Saving user {}", user);
        this.userRepository.save(user);

        final List<User> users = new ArrayList<>();
        users.add(user);
        userRole.setUsers(users);

        this.roleRepository.save(userRole);


    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        final String newAccesToken = this.jwtService.refresAccessToken(request.refreshToken());
        final String tokenType = "Bearer";

        return new AuthenticationResponse(
                newAccesToken,
                request.refreshToken(),
                tokenType
        );

    }


    private void checkPasswords(String password, String confirmPassword) {
        if (password == null || !confirmPassword.equals(password)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void checkUserPhoneNumber(String phoneNumber) {
        final boolean phoneNumberExists = this.userRepository.existsByPhoneNumberIgnoreCase(phoneNumber);
        if (phoneNumberExists) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }
    }

    private void checkUserEmail(String email) {
        final boolean emailExists = this.userRepository.existsByEmailIgnoreCase(email);
        if (emailExists) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}
