package gov.cmr.minfi.db.gbe.app.common.config;

import gov.cmr.minfi.db.gbe.app.affectation.UserAffectation;
import gov.cmr.minfi.db.gbe.app.affectation.UserAffectationRepository;
import gov.cmr.minfi.db.gbe.app.auth.tfa.TwoFactorAuthenticationService;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.exercice.ExerciceRepository;
import gov.cmr.minfi.db.gbe.app.iam.role.Role;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleRepository;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.*;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Action;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ProgrammeRepository;
import gov.cmr.minfi.db.gbe.app.user.User;
import gov.cmr.minfi.db.gbe.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final ProgrammeRepository programmeRepository;
    private final ActionRepository actionRepository;
    private final ChapitreRepository chapitreRepository;
    private final ExerciceRepository exerciceRepository;
    private final UserAffectationRepository affectationRepository;
    private final TwoFactorAuthenticationService tfaService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("!prod")
    public CommandLineRunner init() {
        return args -> {

            // ================================================
            // ÉTAPE 1 — Rôles système
            // ================================================
            for (RoleSysteme rs : RoleSysteme.values()) {
                final String roleName = "ROLE_" + rs.name();
                if (roleRepository.findByName(roleName).isEmpty()) {
                    final Role role = new Role();
                    role.setName(roleName);
                    role.setCreatedBy("SYSTEM");
                    roleRepository.save(role);
                    log.info("Rôle créé : {}", roleName);
                }
            }

            if (userRepository.existsByEmailIgnoreCase("admin@minfi.cm")) {
                log.info("Données déjà présentes — initialisation ignorée");
                return;
            }

            // ================================================
            // ÉTAPE 2 — Exercice budgétaire 2026
            // ================================================
            final Exercice exercice2026 = exerciceRepository.save(
                    Exercice.builder()
                            .annee(2026)
                            .libelleFr("Exercice budgétaire 2026")
                            .libelleEn("Budget exercise 2026")
                            .actif(true)
                            .build()
            );
            log.info("Exercice créé : {}", exercice2026.getAnnee());

            // ================================================
            // ÉTAPE 3 — Sections (Ministères)
            // ================================================

            // Section MINFI — code 20
            final Section sectionMinfi = sectionRepository.save(
                    Section.builder()
                            .codeSection("20")
                            .sigle("MINFI")
                            .libelleFr("Ministère des Finances")
                            .libelleEn("Ministry of Finance")
                            .typeSection(TypeSection.MINISTERE)
                            .exercice(exercice2026)
                            .build()
            );

            // Section MINESEC — code 53
            final Section sectionMinesec = sectionRepository.save(
                    Section.builder()
                            .codeSection("53")
                            .sigle("MINESEC")
                            .libelleFr("Ministère des Enseignements Secondaires")
                            .libelleEn("Ministry of Secondary Education")
                            .typeSection(TypeSection.MINISTERE)
                            .exercice(exercice2026)
                            .build()
            );

            // Section MINSANTE — code 56
            final Section sectionMinsante = sectionRepository.save(
                    Section.builder()
                            .codeSection("56")
                            .sigle("MINSANTE")
                            .libelleFr("Ministère de la Santé Publique")
                            .libelleEn("Ministry of Public Health")
                            .typeSection(TypeSection.MINISTERE)
                            .exercice(exercice2026)
                            .build()
            );

            log.info("Sections créées : MINFI, MINESEC, MINSANTE");

            // ================================================
            // ÉTAPE 4 — Catégories de service (pour les chapitres)
            // ================================================
            final CategorieService catAdminCentrale = new CategorieService();
            catAdminCentrale.setCode("31");
            catAdminCentrale.setLibelleFr("Direction centrale technique");
            catAdminCentrale.setLibelleEn("Central technical directorate");
            catAdminCentrale.setTypeAdministration(TypeAdministration.ADMINISTRATION_CENTRALE);
            catAdminCentrale.setCreatedBy("SYSTEM");
            // Note : si CategorieService n'a pas de repository dédié, adapte selon ton architecture

            // ================================================
            // ÉTAPE 5 — Programmes MINFI
            // ================================================
            final Programme progMinfiPilotage = programmeRepository.save(
                    Programme.builder()
                            .section(sectionMinfi)
                            .exercice(exercice2026)
                            .code("231")
                            .codeMille("60")
                            .autreCode("60.231")
                            .libelleFr("Pilotage et coordination des services du MINFI")
                            .libelleEn("Steering and coordination of MINFI services")
                            .actif(true)
                            .build()
            );

            final Programme progMinfiMobilisation = programmeRepository.save(
                    Programme.builder()
                            .section(sectionMinfi)
                            .exercice(exercice2026)
                            .code("232")
                            .codeMille("60")
                            .autreCode("60.232")
                            .libelleFr("Mobilisation des ressources de l'État")
                            .libelleEn("Mobilization of state resources")
                            .actif(true)
                            .build()
            );

            final Programme progMinfiBudget = programmeRepository.save(
                    Programme.builder()
                            .section(sectionMinfi)
                            .exercice(exercice2026)
                            .code("233")
                            .codeMille("60")
                            .autreCode("60.233")
                            .libelleFr("Gestion budgétaire et financière")
                            .libelleEn("Budget and financial management")
                            .actif(true)
                            .build()
            );

            // Programmes MINESEC
            final Programme progMinesecEnseignement = programmeRepository.save(
                    Programme.builder()
                            .section(sectionMinesec)
                            .exercice(exercice2026)
                            .code("150")
                            .codeMille("60")
                            .autreCode("60.150")
                            .libelleFr("Amélioration de l'accès à l'enseignement secondaire")
                            .libelleEn("Improving access to secondary education")
                            .actif(true)
                            .build()
            );

            final Programme progMinesecQualite = programmeRepository.save(
                    Programme.builder()
                            .section(sectionMinesec)
                            .exercice(exercice2026)
                            .code("151")
                            .codeMille("60")
                            .autreCode("60.151")
                            .libelleFr("Amélioration de la qualité des enseignements secondaires")
                            .libelleEn("Improving the quality of secondary education")
                            .actif(true)
                            .build()
            );

            // Programmes MINSANTE
            final Programme progMinsanteSante = programmeRepository.save(
                    Programme.builder()
                            .section(sectionMinsante)
                            .exercice(exercice2026)
                            .code("183")
                            .codeMille("60")
                            .autreCode("60.183")
                            .libelleFr("Développement des soins de santé")
                            .libelleEn("Development of health care")
                            .actif(true)
                            .build()
            );

            log.info("Programmes créés : 3 MINFI, 2 MINESEC, 1 MINSANTE");

            // ================================================
            // ÉTAPE 6 — Actions
            // ================================================

            // Actions MINFI — Programme Pilotage
            final Action actionMinfiPilotage1 = actionRepository.save(
                    Action.builder()
                            .programme(progMinfiPilotage)
                            .exercice(exercice2026)
                            .codeAction("0")
                            .autreCode("60.231.0")
                            .libelleFr("Pilotage et coordination des actions du Ministère")
                            .libelleEn("Steering and coordination of Ministry actions")
                            .build()
            );

            final Action actionMinfiPilotage2 = actionRepository.save(
                    Action.builder()
                            .programme(progMinfiPilotage)
                            .exercice(exercice2026)
                            .codeAction("1")
                            .autreCode("60.231.1")
                            .libelleFr("Amélioration du cadre de travail et gestion des ressources")
                            .libelleEn("Improvement of working environment and resource management")
                            .build()
            );

            // Actions MINFI — Programme Mobilisation
            final Action actionMinfiMob1 = actionRepository.save(
                    Action.builder()
                            .programme(progMinfiMobilisation)
                            .exercice(exercice2026)
                            .codeAction("1")
                            .autreCode("60.232.1")
                            .libelleFr("Mobilisation des recettes fiscales intérieures")
                            .libelleEn("Mobilization of domestic tax revenues")
                            .build()
            );

            final Action actionMinfiMob2 = actionRepository.save(
                    Action.builder()
                            .programme(progMinfiMobilisation)
                            .exercice(exercice2026)
                            .codeAction("2")
                            .autreCode("60.232.2")
                            .libelleFr("Mobilisation des recettes douanières")
                            .libelleEn("Mobilization of customs revenues")
                            .build()
            );

            // Actions MINFI — Programme Budget
            final Action actionMinfiBudget1 = actionRepository.save(
                    Action.builder()
                            .programme(progMinfiBudget)
                            .exercice(exercice2026)
                            .codeAction("1")
                            .autreCode("60.233.1")
                            .libelleFr("Préparation du budget de l'État")
                            .libelleEn("Preparation of the state budget")
                            .build()
            );

            final Action actionMinfiBudget2 = actionRepository.save(
                    Action.builder()
                            .programme(progMinfiBudget)
                            .exercice(exercice2026)
                            .codeAction("2")
                            .autreCode("60.233.2")
                            .libelleFr("Contrôle budgétaire")
                            .libelleEn("Budgetary control")
                            .build()
            );

            // Actions MINESEC
            final Action actionMinesecAcces1 = actionRepository.save(
                    Action.builder()
                            .programme(progMinesecEnseignement)
                            .exercice(exercice2026)
                            .codeAction("1")
                            .autreCode("60.150.1")
                            .libelleFr("Construction et équipement des établissements secondaires")
                            .libelleEn("Construction and equipment of secondary schools")
                            .build()
            );

            final Action actionMinesecAcces2 = actionRepository.save(
                    Action.builder()
                            .programme(progMinesecEnseignement)
                            .exercice(exercice2026)
                            .codeAction("2")
                            .autreCode("60.150.2")
                            .libelleFr("Recrutement et déploiement des enseignants")
                            .libelleEn("Recruitment and deployment of teachers")
                            .build()
            );

            // Actions MINSANTE
            final Action actionMinsante1 = actionRepository.save(
                    Action.builder()
                            .programme(progMinsanteSante)
                            .exercice(exercice2026)
                            .codeAction("1")
                            .autreCode("60.183.1")
                            .libelleFr("Renforcement de la couverture sanitaire")
                            .libelleEn("Strengthening health coverage")
                            .build()
            );

            log.info("Actions créées : 6 MINFI, 2 MINESEC, 1 MINSANTE");

            // ================================================
            // ÉTAPE 7 — Utilisateurs et affectations
            // ================================================
            final Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            final Role roleOrdPrincipal = roleRepository.findByName("ROLE_ORDONNATEUR_PRINCIPAL").orElseThrow();
            final Role roleOrdSecondaire = roleRepository.findByName("ROLE_ORDONNATEUR_SECONDAIRE").orElseThrow();
            final Role roleOrdDelegue = roleRepository.findByName("ROLE_ORDONNATEUR_DELEGUE").orElseThrow();
            final Role roleCfi = roleRepository.findByName("ROLE_CONTROLEUR_FINANCIER").orElseThrow();
            final Role roleComptable = roleRepository.findByName("ROLE_COMPTABLE").orElseThrow();

            // ── Admin système
            final User admin = userRepository.saveAndFlush(User.builder()
                    .firstName("Super").lastName("Admin")
                    .email("admin@minfi.cm")
                    .phoneNumber("+237600000001")
                    .password(passwordEncoder.encode("Admin@1234"))
                    .matricule("000000001")
                    .numeroCni("CN000000001")
                    .nui("NUI000000001")
                    .cniIssueDate(LocalDate.of(2020, 1, 1))
                    .cniExpiryDate(LocalDate.of(2030, 1, 1))
                    .enabled(true).locked(false).credentialsExpired(false)
                    .emailVerified(true).phoneVerified(true)
                    .firstLogin(false).mfaEnabled(true)
                    .secret(tfaService.generateNewSecret())
                    .role(roleAdmin)
                    .build());

            saveAffectation(admin, sectionMinfi, progMinfiPilotage, RoleSysteme.ADMIN);

            // ── Ordonnateur Principal MINFI
            final User ordPrincipalMinfi = userRepository.saveAndFlush(User.builder()
                    .firstName("Jean-Baptiste").lastName("Mbouck")
                    .email("jb.mbouck@minfi.cm")
                    .phoneNumber("+237600000002")
                    .password(passwordEncoder.encode("Test@1234"))
                    .matricule("000000002")
                    .numeroCni("CN000000002")
                    .nui("NUI000000002")
                    .cniIssueDate(LocalDate.of(2019, 3, 15))
                    .cniExpiryDate(LocalDate.of(2029, 3, 15))
                    .enabled(true).locked(false).credentialsExpired(false)
                    .emailVerified(false).phoneVerified(false)
                    .firstLogin(true).mfaEnabled(false)
                    .secret(tfaService.generateNewSecret())
                    .role(roleOrdPrincipal)
                    .build());

            // Affecté aux 3 programmes MINFI
            saveAffectation(ordPrincipalMinfi, sectionMinfi, progMinfiPilotage, RoleSysteme.ORDONNATEUR_PRINCIPAL);
            saveAffectation(ordPrincipalMinfi, sectionMinfi, progMinfiMobilisation, RoleSysteme.ORDONNATEUR_PRINCIPAL);
            saveAffectation(ordPrincipalMinfi, sectionMinfi, progMinfiBudget, RoleSysteme.ORDONNATEUR_PRINCIPAL);

            // ── Ordonnateur Secondaire MINFI
            final User ordSecondaireMinfi = userRepository.saveAndFlush(User.builder()
                    .firstName("Marie-Claire").lastName("Essomba")
                    .email("mc.essomba@minfi.cm")
                    .phoneNumber("+237600000003")
                    .password(passwordEncoder.encode("Test@1234"))
                    .matricule("000000003")
                    .numeroCni("CN000000003")
                    .nui("NUI000000003")
                    .cniIssueDate(LocalDate.of(2021, 6, 10))
                    .cniExpiryDate(LocalDate.of(2031, 6, 10))
                    .enabled(true).locked(false).credentialsExpired(false)
                    .emailVerified(false).phoneVerified(false)
                    .firstLogin(true).mfaEnabled(false)
                    .secret(tfaService.generateNewSecret())
                    .role(roleOrdSecondaire)
                    .build());

            // Affecté uniquement au programme Budget
            saveAffectation(ordSecondaireMinfi, sectionMinfi, progMinfiBudget, RoleSysteme.ORDONNATEUR_SECONDAIRE);

            // ── Ordonnateur Délégué MINFI
            final User ordDelegueMinfi = userRepository.saveAndFlush(User.builder()
                    .firstName("Paul").lastName("Ndjodo")
                    .email("p.ndjodo@minfi.cm")
                    .phoneNumber("+237600000004")
                    .password(passwordEncoder.encode("Test@1234"))
                    .matricule("000000004")
                    .numeroCni("CN000000004")
                    .nui("NUI000000004")
                    .cniIssueDate(LocalDate.of(2020, 9, 5))
                    .cniExpiryDate(LocalDate.of(2030, 9, 5))
                    .enabled(true).locked(false).credentialsExpired(false)
                    .emailVerified(false).phoneVerified(false)
                    .firstLogin(true).mfaEnabled(false)
                    .secret(tfaService.generateNewSecret())
                    .role(roleOrdDelegue)
                    .build());

            saveAffectation(ordDelegueMinfi, sectionMinfi, progMinfiMobilisation, RoleSysteme.ORDONNATEUR_DELEGUE);

            // ── Contrôleur Financier MINFI
            final User cfiMinfi = userRepository.saveAndFlush(User.builder()
                    .firstName("Hortense").lastName("Bikele")
                    .email("h.bikele@minfi.cm")
                    .phoneNumber("+237600000005")
                    .password(passwordEncoder.encode("Test@1234"))
                    .matricule("000000005")
                    .numeroCni("CN000000005")
                    .nui("NUI000000005")
                    .cniIssueDate(LocalDate.of(2018, 11, 20))
                    .cniExpiryDate(LocalDate.of(2028, 11, 20))
                    .enabled(true).locked(false).credentialsExpired(false)
                    .emailVerified(false).phoneVerified(false)
                    .firstLogin(true).mfaEnabled(false)
                    .secret(tfaService.generateNewSecret())
                    .role(roleCfi)
                    .build());

            saveAffectation(cfiMinfi, sectionMinfi, progMinfiPilotage, RoleSysteme.CONTROLEUR_FINANCIER);
            saveAffectation(cfiMinfi, sectionMinfi, progMinfiBudget, RoleSysteme.CONTROLEUR_FINANCIER);

            // ── Comptable MINFI
            final User comptableMinfi = userRepository.saveAndFlush(User.builder()
                    .firstName("Roger").lastName("Atanga")
                    .email("r.atanga@minfi.cm")
                    .phoneNumber("+237600000006")
                    .password(passwordEncoder.encode("Test@1234"))
                    .matricule("000000006")
                    .numeroCni("CN000000006")
                    .nui("NUI000000006")
                    .cniIssueDate(LocalDate.of(2022, 4, 8))
                    .cniExpiryDate(LocalDate.of(2032, 4, 8))
                    .enabled(true).locked(false).credentialsExpired(false)
                    .emailVerified(false).phoneVerified(false)
                    .firstLogin(true).mfaEnabled(false)
                    .secret(tfaService.generateNewSecret())
                    .role(roleComptable)
                    .build());

            saveAffectation(comptableMinfi, sectionMinfi, progMinfiBudget, RoleSysteme.COMPTABLE);

            // ── Ordonnateur Principal MINESEC
            final User ordPrincipalMinesec = userRepository.saveAndFlush(User.builder()
                    .firstName("Alphonse").lastName("Owona")
                    .email("a.owona@minesec.cm")
                    .phoneNumber("+237600000007")
                    .password(passwordEncoder.encode("Test@1234"))
                    .matricule("000000007")
                    .numeroCni("CN000000007")
                    .nui("NUI000000007")
                    .cniIssueDate(LocalDate.of(2017, 7, 25))
                    .cniExpiryDate(LocalDate.of(2027, 7, 25))
                    .enabled(true).locked(false).credentialsExpired(false)
                    .emailVerified(false).phoneVerified(false)
                    .firstLogin(true).mfaEnabled(false)
                    .secret(tfaService.generateNewSecret())
                    .role(roleOrdPrincipal)
                    .build());

            saveAffectation(ordPrincipalMinesec, sectionMinesec, progMinesecEnseignement, RoleSysteme.ORDONNATEUR_PRINCIPAL);
            saveAffectation(ordPrincipalMinesec, sectionMinesec, progMinesecQualite, RoleSysteme.ORDONNATEUR_PRINCIPAL);

            // ── Contrôleur Financier MINSANTE
            final User cfiMinsante = userRepository.saveAndFlush(User.builder()
                    .firstName("Célestine").lastName("Ndongo")
                    .email("c.ndongo@minsante.cm")
                    .phoneNumber("+237600000008")
                    .password(passwordEncoder.encode("Test@1234"))
                    .matricule("000000008")
                    .numeroCni("CN000000008")
                    .nui("NUI000000008")
                    .cniIssueDate(LocalDate.of(2023, 2, 14))
                    .cniExpiryDate(LocalDate.of(2033, 2, 14))
                    .enabled(true).locked(false).credentialsExpired(false)
                    .emailVerified(false).phoneVerified(false)
                    .firstLogin(true).mfaEnabled(false)
                    .secret(tfaService.generateNewSecret())
                    .role(roleCfi)
                    .build());

            saveAffectation(cfiMinsante, sectionMinsante, progMinsanteSante, RoleSysteme.CONTROLEUR_FINANCIER);

            log.info("================================================");
            log.info("Initialisation terminée avec succès");
            log.info("------------------------------------------------");
            log.info("admin@minfi.cm          / Admin@1234  [ADMIN]");
            log.info("jb.mbouck@minfi.cm      / Test@1234   [ORD. PRINCIPAL MINFI]");
            log.info("mc.essomba@minfi.cm     / Test@1234   [ORD. SECONDAIRE MINFI]");
            log.info("p.ndjodo@minfi.cm       / Test@1234   [ORD. DÉLÉGUÉ MINFI]");
            log.info("h.bikele@minfi.cm       / Test@1234   [CFI MINFI]");
            log.info("r.atanga@minfi.cm       / Test@1234   [COMPTABLE MINFI]");
            log.info("a.owona@minesec.cm      / Test@1234   [ORD. PRINCIPAL MINESEC]");
            log.info("c.ndongo@minsante.cm    / Test@1234   [CFI MINSANTE]");
            log.info("================================================");
        };
    }

    private void saveAffectation(User user, Section section, Programme programme, RoleSysteme roleSysteme) {
        final UserAffectation affectation = UserAffectation.builder()
                .user(user)
                .section(section)
                .programme(programme)
                .roleSysteme(roleSysteme)
                .actif(true)
                .build();
        affectation.initialiserPermissionsDepuisRole();
        affectationRepository.save(affectation);
    }
}