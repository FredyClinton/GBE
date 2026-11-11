package gov.cmr.minfi.db.gbe.app;

import gov.cmr.minfi.db.gbe.app.budget.credit.StatutCredit;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleRepository;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Démarre l'application complète sur un PostgreSQL éphémère (Testcontainers, Docker requis) :
 * vérifie que les migrations Flyway s'appliquent et que le modèle JPA les valide (ddl-auto=validate).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GbeApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.9");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    @Test
    void migrationsAppliquees_referentielEtRolesCharges() {
        assertThat(sectionRepository.count()).isGreaterThan(1);
        assertThat(roleRepository.findByName("ROLE_ADMIN")).isPresent();
        assertThat(roleRepository.count()).isEqualTo(RoleSysteme.values().length);
        assertThat(roleRepository.findByName("ROLE_FOURNISSEUR")).isPresent();
        assertThat(roleRepository.findByName("ROLE_COMPTABLE_MATIERE")).isPresent();
    }

    @Test
    void naturesEconomiques_rattacheesALExercice2026() {
        final Integer sansExercice = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM natures_economiques WHERE exercice_id IS NULL", Integer.class);
        final Integer sur2026 = jdbcTemplate.queryForObject("""
                SELECT count(*) FROM natures_economiques n JOIN exercice e ON e.id = n.exercice_id
                WHERE e.annee = 2026
                """, Integer.class);
        assertThat(sansExercice).isZero();
        assertThat(sur2026).isGreaterThan(1000);
    }

    @Test
    void contraintesCheck_acceptentToutesLesValeursDesEnumerations() {
        final String roles = jdbcTemplate.queryForObject(
                "SELECT pg_get_constraintdef(oid) FROM pg_constraint WHERE conname = 'mandat_role_systeme_check'",
                String.class);
        final String permissions = jdbcTemplate.queryForObject(
                "SELECT pg_get_constraintdef(oid) FROM pg_constraint WHERE conname = 'mandat_permissions_permission_check'",
                String.class);
        for (RoleSysteme r : RoleSysteme.values()) {
            assertThat(roles).contains("'" + r.name() + "'");
        }
        for (Permission p : Permission.values()) {
            assertThat(permissions).contains("'" + p.name() + "'");
        }
        final String statuts = jdbcTemplate.queryForObject(
                "SELECT pg_get_constraintdef(oid) FROM pg_constraint WHERE conname = 'credit_budgetaire_statut_check'",
                String.class);
        for (StatutCredit st : StatutCredit.values()) {
            assertThat(statuts).contains("'" + st.name() + "'");
        }
        assertThat(statuts).doesNotContain("'ENGAGE'", "'SOLDE'");
    }

    @Test
    void superAdminInitial_disposeDeToutesLesPermissions() {
        final List<String> permissions = jdbcTemplate.queryForList("""
                SELECT mp.permission FROM mandat_permissions mp
                JOIN mandat m ON m.id = mp.mandat_id JOIN users u ON u.id = m.user_id
                WHERE u.email = 'admin@gbe.cm'
                """, String.class);
        assertThat(permissions).containsExactlyInAnyOrder(
                Arrays.stream(Permission.values()).map(Enum::name).toArray(String[]::new));
    }

    @Test
    void contrainteUniciteImputationCredit_inclutLaNatureEconomique() {
        final List<String> colonnes = jdbcTemplate.queryForList("""
                SELECT a.attname
                FROM pg_constraint c
                JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
                WHERE c.conname = 'uk_credit_imputation' AND c.contype = 'u'
                """, String.class);
        assertThat(colonnes).containsExactlyInAnyOrder(
                "exercice_id", "section_id", "programme_id", "action_id", "chapitre_id", "nature_economique_id");
    }

    @Test
    void fournisseur_creeParLAdmin_seConnecteSansMandat() throws Exception {
        final String admin = "Bearer " + jwtService.generateAccessToken("admin@gbe.cm");
        mockMvc.perform(post("/api/v1/admin/fournisseurs")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"raisonSociale":"SARL Test","niu":"M000000000001T","telephone":"+237222000001",
                                 "nomRepresentant":"Essomba","prenomRepresentant":"Marie",
                                 "email":"fournisseur.test@exemple.cm","telephoneRepresentant":"+237690000001",
                                 "password":"Temp@1234",
                                 "ribs":["10005 00001 01234567890 12"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ribs[0].rib").value("10005000010123456789012"))
                .andExpect(jsonPath("$.ribs[0].id").isNotEmpty());

        final String fournisseur = "Bearer " + jwtService.generateAccessToken("fournisseur.test@exemple.cm");
        mockMvc.perform(get("/api/v1/fournisseurs/moi").header("Authorization", fournisseur))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.raisonSociale").value("SARL Test"))
                .andExpect(jsonPath("$.telephone").value("+237222000001"))
                .andExpect(jsonPath("$.telephoneRepresentant").value("+237690000001"))
                .andExpect(jsonPath("$.actif").value(true));
        // Pas d'accès aux routes d'administration
        mockMvc.perform(get("/api/v1/admin/fournisseurs").header("Authorization", fournisseur))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE_PERMISSION"));
        // Un agent n'a pas d'espace fournisseur
        mockMvc.perform(get("/api/v1/fournisseurs/moi").header("Authorization", admin))
                .andExpect(status().isForbidden());

        final Integer mandats = jdbcTemplate.queryForObject("""
                SELECT count(*) FROM mandat m JOIN users u ON u.id = m.user_id
                WHERE u.email = 'fournisseur.test@exemple.cm'
                """, Integer.class);
        assertThat(mandats).isZero();
    }

    @Test
    void creationUtilisateur_avecRoleFournisseur_refusee() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + jwtService.generateAccessToken("admin@gbe.cm"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"agentId":"inconnu","email":"x@exemple.cm","password":"Temp@1234",
                                 "roleSysteme":"FOURNISSEUR","sectionId":"inconnue"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ROLE_EXTERNE_SANS_MANDAT"));
    }

    @Test
    void routesProtegees_sansJeton_refusees() throws Exception {
        mockMvc.perform(get("/api/v1/referentiel/sections")).andExpect(status().isForbidden());
    }

    @Test
    void preflightCors_origineAutorisee() throws Exception {
        mockMvc.perform(options("/api/v1/referentiel/sections")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void connexionAdminInitial_premiereConnexion() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@gbe.cm\",\"password\":\"Admin@1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstLogin").value(true));
    }
}
