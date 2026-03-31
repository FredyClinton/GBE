package gov.cmr.minfi.db.gbe.app.common.config;

import gov.cmr.minfi.db.gbe.app.agent.Agent;
import gov.cmr.minfi.db.gbe.app.agent.AgentRepository;
import gov.cmr.minfi.db.gbe.app.auth.tfa.TwoFactorAuthenticationService;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.exercice.ExerciceRepository;
import gov.cmr.minfi.db.gbe.app.iam.role.Role;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleRepository;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.mandat.Mandat;
import gov.cmr.minfi.db.gbe.app.mandat.MandatRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.ChapitreRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.TypeSection;
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
    private final AgentRepository agentRepository;
    private final SectionRepository sectionRepository;
    private final ProgrammeRepository programmeRepository;
    private final ActionRepository actionRepository;
    private final ChapitreRepository chapitreRepository;
    private final ExerciceRepository exerciceRepository;
    private final MandatRepository mandatRepository;
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
            final Section sectionMinfi = sectionRepository.save(
                    Section.builder()
                            .codeSection("20").sigle("MINFI")
                            .libelleFr("Ministère des Finances")
                            .libelleEn("Ministry of Finance")
                            .typeSection(TypeSection.MINISTERE)
                            .exercice(exercice2026)
                            .build()
            );

            final Section sectionMinesec = sectionRepository.save(
                    Section.builder()
                            .codeSection("53").sigle("MINESEC")
                            .libelleFr("Ministère des Enseignements Secondaires")
                            .libelleEn("Ministry of Secondary Education")
                            .typeSection(TypeSection.MINISTERE)
                            .exercice(exercice2026)
                            .build()
            );

            final Section sectionMinsante = sectionRepository.save(
                    Section.builder()
                            .codeSection("56").sigle("MINSANTE")
                            .libelleFr("Ministère de la Santé Publique")
                            .libelleEn("Ministry of Public Health")
                            .typeSection(TypeSection.MINISTERE)
                            .exercice(exercice2026)
                            .build()
            );
            log.info("Sections créées : MINFI, MINESEC, MINSANTE");

            // ================================================
            // ÉTAPE 4 — Programmes
            // ================================================
            final Programme progMinfiPilotage = saveProgram(
                    sectionMinfi, exercice2026, "231", "60.231",
                    "Pilotage et coordination des services du MINFI",
                    "Steering and coordination of MINFI services"
            );
            final Programme progMinfiMobilisation = saveProgram(
                    sectionMinfi, exercice2026, "232", "60.232",
                    "Mobilisation des ressources de l'État",
                    "Mobilization of state resources"
            );
            final Programme progMinfiBudget = saveProgram(
                    sectionMinfi, exercice2026, "233", "60.233",
                    "Gestion budgétaire et financière",
                    "Budget and financial management"
            );
            final Programme progMinesecEnseignement = saveProgram(
                    sectionMinesec, exercice2026, "150", "60.150",
                    "Amélioration de l'accès à l'enseignement secondaire",
                    "Improving access to secondary education"
            );
            final Programme progMinesecQualite = saveProgram(
                    sectionMinesec, exercice2026, "151", "60.151",
                    "Amélioration de la qualité des enseignements secondaires",
                    "Improving the quality of secondary education"
            );
            final Programme progMinsanteSante = saveProgram(
                    sectionMinsante, exercice2026, "183", "60.183",
                    "Développement des soins de santé",
                    "Development of health care"
            );
            log.info("Programmes créés : 3 MINFI, 2 MINESEC, 1 MINSANTE");

            // ================================================
            // ÉTAPE 5 — Actions
            // ================================================
            actionRepository.save(Action.builder()
                    .programme(progMinfiPilotage).exercice(exercice2026)
                    .codeAction("0").autreCode("60.231.0")
                    .libelleFr("Pilotage et coordination des actions du Ministère")
                    .libelleEn("Steering and coordination of Ministry actions")
                    .build());
            actionRepository.save(Action.builder()
                    .programme(progMinfiPilotage).exercice(exercice2026)
                    .codeAction("1").autreCode("60.231.1")
                    .libelleFr("Amélioration du cadre de travail et gestion des ressources")
                    .libelleEn("Improvement of working environment and resource management")
                    .build());
            actionRepository.save(Action.builder()
                    .programme(progMinfiMobilisation).exercice(exercice2026)
                    .codeAction("1").autreCode("60.232.1")
                    .libelleFr("Mobilisation des recettes fiscales intérieures")
                    .libelleEn("Mobilization of domestic tax revenues")
                    .build());
            actionRepository.save(Action.builder()
                    .programme(progMinfiMobilisation).exercice(exercice2026)
                    .codeAction("2").autreCode("60.232.2")
                    .libelleFr("Mobilisation des recettes douanières")
                    .libelleEn("Mobilization of customs revenues")
                    .build());
            actionRepository.save(Action.builder()
                    .programme(progMinfiBudget).exercice(exercice2026)
                    .codeAction("1").autreCode("60.233.1")
                    .libelleFr("Préparation du budget de l'État")
                    .libelleEn("Preparation of the state budget")
                    .build());
            actionRepository.save(Action.builder()
                    .programme(progMinfiBudget).exercice(exercice2026)
                    .codeAction("2").autreCode("60.233.2")
                    .libelleFr("Contrôle budgétaire")
                    .libelleEn("Budgetary control")
                    .build());
            actionRepository.save(Action.builder()
                    .programme(progMinesecEnseignement).exercice(exercice2026)
                    .codeAction("1").autreCode("60.150.1")
                    .libelleFr("Construction et équipement des établissements secondaires")
                    .libelleEn("Construction and equipment of secondary schools")
                    .build());
            actionRepository.save(Action.builder()
                    .programme(progMinesecEnseignement).exercice(exercice2026)
                    .codeAction("2").autreCode("60.150.2")
                    .libelleFr("Recrutement et déploiement des enseignants")
                    .libelleEn("Recruitment and deployment of teachers")
                    .build());
            actionRepository.save(Action.builder()
                    .programme(progMinsanteSante).exercice(exercice2026)
                    .codeAction("1").autreCode("60.183.1")
                    .libelleFr("Renforcement de la couverture sanitaire")
                    .libelleEn("Strengthening health coverage")
                    .build());
            log.info("Actions créées : 6 MINFI, 2 MINESEC, 1 MINSANTE");

            // ================================================
            // ÉTAPE 6 — Rôles IAM
            // ================================================
            final Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
            final Role roleOrdonnateur = roleRepository.findByName("ROLE_ORDONNATEUR").orElseThrow();
            final Role roleMinistre = roleRepository.findByName("ROLE_MINISTRE").orElseThrow();
            final Role roleCfi = roleRepository.findByName("ROLE_CONTROLEUR_FINANCIER").orElseThrow();
            final Role roleComptable = roleRepository.findByName("ROLE_COMPTABLE").orElseThrow();
            final Role roleGestionnaire = roleRepository.findByName("ROLE_GESTIONNAIRE").orElseThrow();
            final Role roleAgent = roleRepository.findByName("ROLE_AGENT").orElseThrow();

            // ================================================
            // ÉTAPE 7 — Agents + Users + Affectations
            // ================================================

            // ── Admin système
            final Agent agentAdmin = saveAgent(
                    "Super", "Admin", "000000001",
                    "NUI000000001", "CN000000001", "+237600000001",
                    LocalDate.of(2020, 1, 1), LocalDate.of(2030, 1, 1)
            );
            final User admin = saveUser(
                    "admin@minfi.cm", "Admin@1234",
                    agentAdmin, roleAdmin, false, true
            );
            saveMandat(admin, sectionMinfi, progMinfiPilotage, RoleSysteme.ADMIN);

            // ── Ordonnateur MINFI (ex Principal)
            final Agent agentOrdMinfi1 = saveAgent(
                    "Jean-Baptiste", "Mbouck", "000000002",
                    "NUI000000002", "CN000000002", "+237600000002",
                    LocalDate.of(2019, 3, 15), LocalDate.of(2029, 3, 15)
            );
            final User ordMinfi1 = saveUser(
                    "jb.mbouck@minfi.cm", "Test@1234",
                    agentOrdMinfi1, roleOrdonnateur, true, false
            );
            saveMandat(ordMinfi1, sectionMinfi, progMinfiPilotage, RoleSysteme.ORDONNATEUR);
            saveMandat(ordMinfi1, sectionMinfi, progMinfiMobilisation, RoleSysteme.ORDONNATEUR);
            saveMandat(ordMinfi1, sectionMinfi, progMinfiBudget, RoleSysteme.ORDONNATEUR);

            // ── Ordonnateur MINFI (ex Secondaire)
            final Agent agentOrdMinfi2 = saveAgent(
                    "Marie-Claire", "Essomba", "000000003",
                    "NUI000000003", "CN000000003", "+237600000003",
                    LocalDate.of(2021, 6, 10), LocalDate.of(2031, 6, 10)
            );
            final User ordMinfi2 = saveUser(
                    "mc.essomba@minfi.cm", "Test@1234",
                    agentOrdMinfi2, roleOrdonnateur, true, false
            );
            saveMandat(ordMinfi2, sectionMinfi, progMinfiBudget, RoleSysteme.ORDONNATEUR);

            // ── Ordonnateur MINFI (ex Délégué)
            final Agent agentOrdMinfi3 = saveAgent(
                    "Paul", "Ndjodo", "000000004",
                    "NUI000000004", "CN000000004", "+237600000004",
                    LocalDate.of(2020, 9, 5), LocalDate.of(2030, 9, 5)
            );
            final User ordMinfi3 = saveUser(
                    "p.ndjodo@minfi.cm", "Test@1234",
                    agentOrdMinfi3, roleOrdonnateur, true, false
            );
            saveMandat(ordMinfi3, sectionMinfi, progMinfiMobilisation, RoleSysteme.ORDONNATEUR);

            // ── Contrôleur Financier MINFI
            final Agent agentCfiMinfi = saveAgent(
                    "Hortense", "Bikele", "000000005",
                    "NUI000000005", "CN000000005", "+237600000005",
                    LocalDate.of(2018, 11, 20), LocalDate.of(2028, 11, 20)
            );
            final User cfiMinfi = saveUser(
                    "h.bikele@minfi.cm", "Test@1234",
                    agentCfiMinfi, roleCfi, true, false
            );
            saveMandat(cfiMinfi, sectionMinfi, progMinfiPilotage, RoleSysteme.CONTROLEUR_FINANCIER);
            saveMandat(cfiMinfi, sectionMinfi, progMinfiBudget, RoleSysteme.CONTROLEUR_FINANCIER);

            // ── Comptable MINFI
            final Agent agentComptable = saveAgent(
                    "Roger", "Atanga", "000000006",
                    "NUI000000006", "CN000000006", "+237600000006",
                    LocalDate.of(2022, 4, 8), LocalDate.of(2032, 4, 8)
            );
            final User comptableMinfi = saveUser(
                    "r.atanga@minfi.cm", "Test@1234",
                    agentComptable, roleComptable, true, false
            );
            saveMandat(comptableMinfi, sectionMinfi, progMinfiBudget, RoleSysteme.COMPTABLE);

            // ── Ordonnateur Principal MINESEC
            final Agent agentOrdMinesec = saveAgent(
                    "Alphonse", "Owona", "000000007",
                    "NUI000000007", "CN000000007", "+237600000007",
                    LocalDate.of(2017, 7, 25), LocalDate.of(2027, 7, 25)
            );
            final User ordMinesec = saveUser(
                    "a.owona@minesec.cm", "Test@1234",
                    agentOrdMinesec, roleOrdonnateur, true, false
            );
            saveMandat(ordMinesec, sectionMinesec, progMinesecEnseignement, RoleSysteme.ORDONNATEUR);
            saveMandat(ordMinesec, sectionMinesec, progMinesecQualite, RoleSysteme.ORDONNATEUR);

            // ── Contrôleur Financier MINSANTE
            final Agent agentCfiMinsante = saveAgent(
                    "Célestine", "Ndongo", "000000008",
                    "NUI000000008", "CN000000008", "+237600000008",
                    LocalDate.of(2023, 2, 14), LocalDate.of(2033, 2, 14)
            );
            final User cfiMinsante = saveUser(
                    "c.ndongo@minsante.cm", "Test@1234",
                    agentCfiMinsante, roleCfi, true, false
            );
            saveMandat(cfiMinsante, sectionMinsante, progMinsanteSante, RoleSysteme.CONTROLEUR_FINANCIER);

            // ── Ministre MINFI (nouveau rôle)
            final Agent agentMinistre = saveAgent(
                    "Emmanuel", "Nganou", "000000009",
                    "NUI000000009", "CN000000009", "+237600000009",
                    LocalDate.of(2015, 5, 10), LocalDate.of(2025, 5, 10)
            );
            final User ministre = saveUser(
                    "e.nganou@minfi.cm", "Test@1234",
                    agentMinistre, roleMinistre, true, false
            );
            // Le Ministre n'a pas de programme — mandat section entière (programme null)
            saveMandatSectionOnly(ministre, sectionMinfi, RoleSysteme.MINISTRE);

            // ── Gestionnaire MINFI (nouveau rôle)
            final Agent agentGestionnaire = saveAgent(
                    "Brigitte", "Fouda", "000000010",
                    "NUI000000010", "CN000000010", "+237600000010",
                    LocalDate.of(2016, 8, 22), LocalDate.of(2026, 8, 22)
            );
            final User gestionnaire = saveUser(
                    "b.fouda@minfi.cm", "Test@1234",
                    agentGestionnaire, roleGestionnaire, true, false
            );
            saveMandat(gestionnaire, sectionMinfi, progMinfiPilotage, RoleSysteme.GESTIONNAIRE);

            log.info("================================================");
            log.info("Initialisation terminée avec succès");
            log.info("------------------------------------------------");
            log.info("admin@minfi.cm        / Admin@1234  [ADMIN]");
            log.info("jb.mbouck@minfi.cm    / Test@1234   [ORDONNATEUR MINFI - 3 programmes]");
            log.info("mc.essomba@minfi.cm   / Test@1234   [ORDONNATEUR MINFI - Budget]");
            log.info("p.ndjodo@minfi.cm     / Test@1234   [ORDONNATEUR MINFI - Mobilisation]");
            log.info("h.bikele@minfi.cm     / Test@1234   [CFI MINFI]");
            log.info("r.atanga@minfi.cm     / Test@1234   [COMPTABLE MINFI]");
            log.info("a.owona@minesec.cm    / Test@1234   [ORDONNATEUR MINESEC]");
            log.info("c.ndongo@minsante.cm  / Test@1234   [CFI MINSANTE]");
            log.info("e.nganou@minfi.cm     / Test@1234   [MINISTRE MINFI]");
            log.info("b.fouda@minfi.cm      / Test@1234   [GESTIONNAIRE MINFI]");
            log.info("================================================");
        };
    }

    // ── Helpers privés ────────────────────────────────────────────────────────

    private Agent saveAgent(
            String firstName, String lastName,
            String matricule, String nui, String numeroCni,
            String phoneNumber,
            LocalDate cniIssueDate, LocalDate cniExpiryDate) {
        return agentRepository.save(Agent.builder()
                .firstName(firstName)
                .lastName(lastName)
                .matricule(matricule)
                .nui(nui)
                .numeroCni(numeroCni)
                .phoneNumber(phoneNumber)
                .cniIssueDate(cniIssueDate)
                .cniExpiryDate(cniExpiryDate)
                .actif(true)
                .build());
    }

    private User saveUser(
            String email, String rawPassword,
            Agent agent, Role role,
            boolean firstLogin, boolean mfaEnabled) {
        return userRepository.saveAndFlush(User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                // Colonnes User maintenues — synchronisées avec l'Agent
                .firstName(agent.getFirstName())
                .lastName(agent.getLastName())
                .matricule(agent.getMatricule())
                .nui(agent.getNui())
                .numeroCni(agent.getNumeroCni())
                .phoneNumber(agent.getPhoneNumber())
                .cniIssueDate(agent.getCniIssueDate())
                .cniExpiryDate(agent.getCniExpiryDate())
                .agent(agent)
                .role(role)
                .enabled(true)
                .locked(false)
                .credentialsExpired(false)
                .emailVerified(!firstLogin)
                .phoneVerified(!firstLogin)
                .firstLogin(firstLogin)
                .mfaEnabled(mfaEnabled)
                .secret(tfaService.generateNewSecret())
                .build());
    }

    private Programme saveProgram(
            Section section, Exercice exercice,
            String code, String autreCode,
            String libelleFr, String libelleEn) {
        return programmeRepository.save(Programme.builder()
                .section(section).exercice(exercice)
                .code(code).autreCode(autreCode).codeMille("60")
                .libelleFr(libelleFr).libelleEn(libelleEn)
                .actif(true)
                .build());
    }

    // Remplacer saveAffectation et saveAffectationSectionOnly
// par ces deux helpers

    private void saveMandat(User user, Section section,
                            Programme programme, RoleSysteme roleSysteme) {
        final Mandat mandat = Mandat.builder()
                .user(user)
                .section(section)
                .programme(programme)
                .roleSysteme(roleSysteme)
                .dateDebut(LocalDate.now())
                .actif(true)
                .build();
        mandat.initialiserPermissionsDepuisRole();
        mandatRepository.save(mandat);
    }

    private void saveMandatSectionOnly(User user, Section section,
                                       RoleSysteme roleSysteme) {
        final Mandat mandat = Mandat.builder()
                .user(user)
                .section(section)
                .programme(null)
                .roleSysteme(roleSysteme)
                .dateDebut(LocalDate.now())
                .actif(true)
                .build();
        mandat.initialiserPermissionsDepuisRole();
        mandatRepository.save(mandat);
    }
}