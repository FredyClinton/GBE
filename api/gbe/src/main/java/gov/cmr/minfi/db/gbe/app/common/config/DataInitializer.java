package gov.cmr.minfi.db.gbe.app.common.config;

import gov.cmr.minfi.db.gbe.app.auth.tfa.TwoFactorAuthenticationService;
import gov.cmr.minfi.db.gbe.app.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.iam.role.Role;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleRepository;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.mandat.Mandat;
import gov.cmr.minfi.db.gbe.app.mandat.MandatRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.user.User;
import gov.cmr.minfi.db.gbe.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@gbe.cm";
    private static final String ADMIN_PASSWORD = "Admin@1234";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SectionRepository sectionRepository;
    private final MandatRepository mandatRepository;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorAuthenticationService tfaService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)) {
            log.debug("DataInitializer — admin déjà présent, skip.");
            return;
        }

        final Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
            .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        // Section bootstrap — portée section entière pour le mandat admin
        final Section section = Section.builder()
            .codeSection("00")
            .sigle("SYS")
            .libelleFr("Administration Système")
            .libelleEn("System Administration")
            .build();
        sectionRepository.save(section);

        // Création du compte super-admin
        final User admin = User.builder()
            .firstName("Super")
            .lastName("Admin")
            .email(ADMIN_EMAIL)
            .phoneNumber("+237600000000")
            .password(passwordEncoder.encode(ADMIN_PASSWORD))
            .role(roleAdmin)
            .enabled(true)
            .locked(false)
            .credentialsExpired(false)
            .emailVerified(false)
            .phoneVerified(false)
            .firstLogin(true)
            .secret(tfaService.generateNewSecret())
            .build();
        userRepository.save(admin);

        // Mandat section-level (programme=null → portée MINISTRE) avec toutes les permissions
        final Mandat mandat = Mandat.builder()
            .user(admin)
            .section(section)
            .roleSysteme(RoleSysteme.ADMIN)
            .dateDebut(LocalDate.now())
            .actif(true)
            .build();
        mandat.setPermissions(new HashSet<>(EnumSet.allOf(Permission.class)));
        mandatRepository.save(mandat);

        log.info("┌─────────────────────────────────────────────┐");
        log.info("│  Super-admin initialisé                     │");
        log.info("│  Email    : {}            │", ADMIN_EMAIL);
        log.info("│  Password : {}                      │", ADMIN_PASSWORD);
        log.info("│  Rôle     : ADMIN + toutes les permissions  │");
        log.info("└─────────────────────────────────────────────┘");
    }
}
