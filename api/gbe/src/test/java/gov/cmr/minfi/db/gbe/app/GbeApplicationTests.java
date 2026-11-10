package gov.cmr.minfi.db.gbe.app;

import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
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

    @Test
    void migrationsAppliquees_referentielEtRolesCharges() {
        assertThat(sectionRepository.count()).isGreaterThan(1);
        assertThat(roleRepository.findByName("ROLE_ADMIN")).isPresent();
        assertThat(roleRepository.count()).isEqualTo(7);
    }

    @Test
    void contrainteUniciteImputationCredit_presenteSurLes5Colonnes() {
        final List<String> colonnes = jdbcTemplate.queryForList("""
                SELECT a.attname
                FROM pg_constraint c
                JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
                WHERE c.conname = 'uk_credit_imputation' AND c.contype = 'u'
                """, String.class);
        assertThat(colonnes).containsExactlyInAnyOrder(
                "exercice_id", "section_id", "programme_id", "action_id", "chapitre_id");
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
