package gov.cmr.minfi.db.gbe.app.identite.user.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatRepository;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import gov.cmr.minfi.db.gbe.app.identite.user.UserMapper;
import gov.cmr.minfi.db.gbe.app.identite.user.UserRepository;
import gov.cmr.minfi.db.gbe.app.identite.user.UserService;
import gov.cmr.minfi.db.gbe.app.identite.user.dto.ChangePasswordRequest;
import gov.cmr.minfi.db.gbe.app.identite.user.dto.ProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final MandatRepository affectationRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(final String userEmail) throws UsernameNotFoundException {
        final User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found : " + userEmail
                ));

        // Charger les permissions actives depuis les mandats
        final Set<Permission> permissions = affectationRepository
                .findMandatsValidesParUser(user.getId())
                .stream()
                .flatMap(affectation -> affectation.getPermissions().stream())
                .collect(Collectors.toCollection(HashSet::new));

        // Rôle externe (fournisseur) : pas de mandat, permissions par défaut du rôle
        roleSystemeDe(user)
                .filter(RoleSysteme::isExterne)
                .ifPresent(role -> permissions.addAll(role.getDefaultPermissions()));

        user.setGrantedPermissions(permissions);
        log.debug("User {} loaded with {} permission(s)", userEmail, permissions.size());

        return user;
    }

    private static Optional<RoleSysteme> roleSystemeDe(User user) {
        if (user.getRole() == null || !user.getRole().getName().startsWith("ROLE_")) {
            return Optional.empty();
        }
        try {
            return Optional.of(RoleSysteme.valueOf(user.getRole().getName().substring("ROLE_".length())));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateProfileInfo(ProfileUpdateRequest request, String userId) {
        User savedUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
        this.userMapper.mergeUserInfo(savedUser, request);
        this.userRepository.save(savedUser);
    }

    @Override
    public void changePassword(ChangePasswordRequest request, String userId) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.CHANGE_PASSWORD_MISMATCH);
        }
        User savedUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
        if (!this.passwordEncoder.matches(request.currentPassword(), savedUser.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }
        savedUser.setPassword(passwordEncoder.encode(request.newPassword()));
        this.userRepository.save(savedUser);
    }

    @Override
    public void deactivateAccount(String userId) {
        final User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DEACTIVATED, userId);
        }
        user.setEnabled(false);
        this.userRepository.save(user);
    }

    @Override
    public void reactivateAccount(String userId) {
        final User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
        if (user.isEnabled()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_ACTIVATED);
        }
        user.setEnabled(true);
        this.userRepository.save(user);
    }

    @Override
    public void deleteAccount(String userId) {
        // TODO: implémenter la méthode
    }
}
