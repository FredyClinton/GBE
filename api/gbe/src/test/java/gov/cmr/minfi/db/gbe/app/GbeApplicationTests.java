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

import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    /** Crée un ordonnateur avec un mandat sur le programme et la permission EXPRIMER_BESOIN ; renvoie son jeton. */
    private String ordonnateur(String email, String telephone, String sectionId, String programmeId) {
        final String userId = UUID.randomUUID().toString();
        final String mandatId = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                INSERT INTO users (id, created_date, first_name, last_name, email, phone_number, password, role_id,
                                   is_enabled, is_account_locked, credentials_expired, is_email_verified,
                                   phone_verified, first_login, mfa_enabled)
                VALUES (?, CURRENT_DATE, 'Test', 'Ordonnateur', ?, ?, 'x',
                        (SELECT id FROM roles WHERE name = 'ROLE_ORDONNATEUR'),
                        true, false, false, false, false, false, false)
                """, userId, email, telephone);
        jdbcTemplate.update("""
                INSERT INTO mandat (id, created_by, created_date, actif, start_mandat, role_systeme,
                                    section_id, programme_id, user_id)
                VALUES (?, 'TEST', CURRENT_DATE, true, CURRENT_DATE, 'ORDONNATEUR', ?, ?, ?)
                """, mandatId, sectionId, programmeId, userId);
        jdbcTemplate.update("INSERT INTO mandat_permissions (mandat_id, permission) VALUES (?, 'EXPRIMER_BESOIN')",
                mandatId);
        return "Bearer " + jwtService.generateAccessToken(email);
    }

    private static String champ(MvcResult resultat, String chemin) throws Exception {
        return JsonPath.read(resultat.getResponse().getContentAsString(), chemin);
    }

    @Test
    void expressionBesoin_creeeEtPublieeParLOrdonnateur_dansSonPerimetre() throws Exception {
        final Map<String, Object> imputation = jdbcTemplate.queryForMap("""
                SELECT a.id AS action_id, a.programme_id, a.section_id,
                       (SELECT c.id FROM chapitres c WHERE c.section_id = a.section_id LIMIT 1) AS chapitre_id,
                       (SELECT n.id FROM natures_economiques n WHERE n.exercice_id = a.exercice_id LIMIT 1) AS nature_id
                FROM actions a
                WHERE a.section_id IS NOT NULL AND a.exercice_id IS NOT NULL
                  AND EXISTS (SELECT 1 FROM chapitres c WHERE c.section_id = a.section_id)
                ORDER BY a.id LIMIT 1
                """);
        final Map<String, Object> ailleurs = jdbcTemplate.queryForMap(
                "SELECT id AS programme_id, section_id FROM programmes WHERE section_id <> ? LIMIT 1",
                imputation.get("section_id"));

        final String admin = "Bearer " + jwtService.generateAccessToken("admin@gbe.cm");
        final String ordo = ordonnateur("ordo.besoin@minfi.cm", "+237670000001",
                (String) imputation.get("section_id"), (String) imputation.get("programme_id"));
        final String horsPerimetre = ordonnateur("ordo.ailleurs@minfi.cm", "+237670000002",
                (String) ailleurs.get("section_id"), (String) ailleurs.get("programme_id"));

        // Référentiel : activité, tâche, matière (par l'admin)
        final String activiteId = champ(mockMvc.perform(post("/api/v1/admin/activites").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"T01\",\"libelle\":\"Activité test\",\"actionId\":\"%s\"}"
                                .formatted(imputation.get("action_id"))))
                .andExpect(status().isCreated()).andReturn(), "$.id");
        final String tacheId = champ(mockMvc.perform(post("/api/v1/admin/taches").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"T01\",\"libelle\":\"Tâche test\",\"activiteId\":\"%s\"}"
                                .formatted(activiteId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.programmeId").value(imputation.get("programme_id")))
                .andReturn(), "$.id");
        final String matiereId = champ(mockMvc.perform(post("/api/v1/admin/matieres").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reference\":\"PAP-A4-TEST\",\"libelle\":\"Papier A4\"}"))
                .andExpect(status().isCreated()).andReturn(), "$.id");

        final String corps = """
                {"objet":"Fournitures de bureau","dateClotureSoumission":"2099-01-01T12:00:00",
                 "tacheId":"%s","chapitreId":"%s","natureEconomiqueId":"%s",
                 "lignes":[{"matiereId":"%s","quantiteDemandee":10,"specifications":"rames de 500 feuilles"}]}
                """.formatted(tacheId, imputation.get("chapitre_id"), imputation.get("nature_id"), matiereId);

        // Hors périmètre : création refusée
        mockMvc.perform(post("/api/v1/besoins").header("Authorization", horsPerimetre)
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE_HORS_PERIMETRE"));

        final String besoinId = champ(mockMvc.perform(post("/api/v1/besoins").header("Authorization", ordo)
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("BROUILLON"))
                .andExpect(jsonPath("$.reference").value(org.hamcrest.Matchers.startsWith("EB-2026-")))
                .andExpect(jsonPath("$.lignes[0].numeroOrdre").value(1))
                .andReturn(), "$.id");

        mockMvc.perform(patch("/api/v1/besoins/{id}/publier", besoinId).header("Authorization", ordo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("PUBLIEE"));
        mockMvc.perform(patch("/api/v1/besoins/{id}/cloturer", besoinId).header("Authorization", ordo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BESOIN_CLOTURE_PREMATUREE"));

        // Lecture : dans le périmètre oui, ailleurs non
        mockMvc.perform(get("/api/v1/besoins").param("statut", "PUBLIEE").param("size", "5")
                        .header("Authorization", ordo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(besoinId)).exists())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/besoins").header("Authorization", horsPerimetre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/api/v1/besoins").param("sort", "objet,asc").header("Authorization", ordo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TRI_INVALIDE"));
        mockMvc.perform(get("/api/v1/besoins/{id}", besoinId).header("Authorization", horsPerimetre))
                .andExpect(status().isForbidden());

        // Consultation publique (UC02) : sans jeton, recherche par mot-clé sur les matières
        mockMvc.perform(get("/api/v1/public/besoins").param("motCle", "PAPIER A4").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(besoinId)).exists())
                .andExpect(jsonPath("$.content[0].tacheId").doesNotExist())
                .andExpect(jsonPath("$.content[0].lignes").doesNotExist())
                .andExpect(jsonPath("$.size").value(100));
        mockMvc.perform(get("/api/v1/public/besoins").param("motCle", "introuvable-xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/api/v1/public/besoins").param("dateLimiteAvant", "2098-12-31T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(besoinId)).doesNotExist());
        mockMvc.perform(get("/api/v1/public/besoins/{id}", besoinId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value(org.hamcrest.Matchers.startsWith("EB-2026-")))
                .andExpect(jsonPath("$.structureEmettrice").isNotEmpty())
                .andExpect(jsonPath("$.lignes[0].matiereLibelle").value("Papier A4"));
        // La consultation publique est en lecture seule
        mockMvc.perform(post("/api/v1/public/besoins").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        // Brouillon supprimé logiquement : il disparaît mais reste en base
        final String brouillonId = champ(mockMvc.perform(post("/api/v1/besoins").header("Authorization", ordo)
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated()).andReturn(), "$.id");
        // Un brouillon n'est pas visible du public
        mockMvc.perform(get("/api/v1/public/besoins/{id}", brouillonId)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/besoins/{id}", brouillonId).header("Authorization", ordo))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/besoins/{id}", brouillonId).header("Authorization", ordo))
                .andExpect(status().isNotFound());
        assertThat(jdbcTemplate.queryForObject("SELECT supprime FROM expressions_besoin WHERE id = ?",
                Boolean.class, brouillonId)).isTrue();
    }

    /** Fournisseur créé par l'admin avec un RIB ; renvoie [jeton, id du RIB]. */
    private String[] fournisseurAvecRib(String niu, String email, String telephone, String rib) throws Exception {
        final String admin = "Bearer " + jwtService.generateAccessToken("admin@gbe.cm");
        final MvcResult r = mockMvc.perform(post("/api/v1/admin/fournisseurs").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"raisonSociale":"SARL %1$s","niu":"%1$s","telephone":"+237222%3$s",
                                 "nomRepresentant":"Nom","prenomRepresentant":"Prénom","email":"%2$s",
                                 "telephoneRepresentant":"+237699%3$s","password":"Temp@1234","ribs":["%4$s"]}
                                """.formatted(niu, email, telephone, rib)))
                .andExpect(status().isCreated()).andReturn();
        return new String[]{"Bearer " + jwtService.generateAccessToken(email), champ(r, "$.ribs[0].id")};
    }

    @Test
    void soumission_deposeeParLeFournisseur_offreTotaleEtRecepisse() throws Exception {
        final Map<String, Object> imputation = jdbcTemplate.queryForMap("""
                SELECT a.id AS action_id, a.programme_id, a.section_id,
                       (SELECT c.id FROM chapitres c WHERE c.section_id = a.section_id LIMIT 1) AS chapitre_id,
                       (SELECT n.id FROM natures_economiques n WHERE n.exercice_id = a.exercice_id LIMIT 1) AS nature_id
                FROM actions a
                WHERE a.section_id IS NOT NULL AND a.exercice_id IS NOT NULL
                  AND EXISTS (SELECT 1 FROM chapitres c WHERE c.section_id = a.section_id)
                ORDER BY a.id DESC LIMIT 1
                """);
        final String admin = "Bearer " + jwtService.generateAccessToken("admin@gbe.cm");
        final String ordo = ordonnateur("ordo.soumission@minfi.cm", "+237670000011",
                (String) imputation.get("section_id"), (String) imputation.get("programme_id"));
        final String[] frs = fournisseurAvecRib("M000000000011S", "frs.un@exemple.cm", "000011",
                "10005000010000000001112");
        final String[] autre = fournisseurAvecRib("M000000000012S", "frs.deux@exemple.cm", "000012",
                "10005000010000000001213");

        // Besoin publié de deux lignes
        final String activiteId = champ(mockMvc.perform(post("/api/v1/admin/activites").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"S01\",\"libelle\":\"A\",\"actionId\":\"%s\"}"
                        .formatted(imputation.get("action_id")))).andExpect(status().isCreated()).andReturn(), "$.id");
        final String tacheId = champ(mockMvc.perform(post("/api/v1/admin/taches").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"S01\",\"libelle\":\"T\",\"activiteId\":\"%s\"}"
                        .formatted(activiteId))).andExpect(status().isCreated()).andReturn(), "$.id");
        final String m1 = champ(mockMvc.perform(post("/api/v1/admin/matieres").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reference\":\"SOU-1\",\"libelle\":\"Ramette\"}"))
                .andExpect(status().isCreated()).andReturn(), "$.id");
        final String m2 = champ(mockMvc.perform(post("/api/v1/admin/matieres").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reference\":\"SOU-2\",\"libelle\":\"Encre\"}"))
                .andExpect(status().isCreated()).andReturn(), "$.id");
        final String corpsBesoin = """
                {"objet":"Fournitures","dateClotureSoumission":"2099-01-01T12:00:00","tacheId":"%s","chapitreId":"%s",
                 "natureEconomiqueId":"%s","lignes":[{"matiereId":"%s","quantiteDemandee":10},
                 {"matiereId":"%s","quantiteDemandee":2.5}]}
                """.formatted(tacheId, imputation.get("chapitre_id"), imputation.get("nature_id"), m1, m2);
        final MvcResult besoin = mockMvc.perform(post("/api/v1/besoins").header("Authorization", ordo)
                .contentType(MediaType.APPLICATION_JSON).content(corpsBesoin)).andExpect(status().isCreated()).andReturn();
        final String besoinId = champ(besoin, "$.id");
        final String l1 = champ(besoin, "$.lignes[0].id");
        final String l2 = champ(besoin, "$.lignes[1].id");
        mockMvc.perform(patch("/api/v1/besoins/{id}/publier", besoinId).header("Authorization", ordo))
                .andExpect(status().isOk());

        final String offre = """
                {"besoinId":"%s","coordonneeBancaireId":"%s","delaiProposeJours":30,"montantProposeTTC":%s,
                 "lignes":[{"ligneBesoinId":"%s","prixUnitairePropose":3500},%s]}
                """;
        final String ligne2 = "{\"ligneBesoinId\":\"%s\",\"prixUnitairePropose\":1000.40}".formatted(l2);

        // Offre partielle refusée ; un agent n'a pas la permission SOUMISSIONNER
        mockMvc.perform(post("/api/v1/soumissions").header("Authorization", frs[0]).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"besoinId":"%s","coordonneeBancaireId":"%s","delaiProposeJours":30,"montantProposeTTC":50000,
                                 "lignes":[{"ligneBesoinId":"%s","prixUnitairePropose":3500}]}
                                """.formatted(besoinId, frs[1], l1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SOUMISSION_INCOMPLETE"));
        mockMvc.perform(post("/api/v1/soumissions").header("Authorization", ordo).contentType(MediaType.APPLICATION_JSON)
                        .content(offre.formatted(besoinId, frs[1], "50000", l1, ligne2)))
                .andExpect(status().isForbidden());
        // RIB d'un autre fournisseur refusé
        mockMvc.perform(post("/api/v1/soumissions").header("Authorization", frs[0]).contentType(MediaType.APPLICATION_JSON)
                        .content(offre.formatted(besoinId, autre[1], "50000", l1, ligne2)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RIB_NOT_FOUND"));
        // TTC inférieur au HT refusé
        mockMvc.perform(post("/api/v1/soumissions").header("Authorization", frs[0]).contentType(MediaType.APPLICATION_JSON)
                        .content(offre.formatted(besoinId, frs[1], "100", l1, ligne2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MONTANT_TTC_INVALIDE"));

        // Dépôt : récépissé numéroté, horodaté, scellé ; HT calculé par le serveur
        final MvcResult depot = mockMvc.perform(post("/api/v1/soumissions").header("Authorization", frs[0])
                        .contentType(MediaType.APPLICATION_JSON).content(offre.formatted(besoinId, frs[1], "44700", l1, ligne2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(org.hamcrest.Matchers.startsWith("SO-2026-")))
                .andExpect(jsonPath("$.statut").value("DEPOSEE"))
                .andExpect(jsonPath("$.montantProposeHT").value(37501.00))
                .andExpect(jsonPath("$.montantProposeTTC").value(44700))
                .andExpect(jsonPath("$.rib").value("10005000010000000001112"))
                .andExpect(jsonPath("$.empreinte").value(org.hamcrest.Matchers.matchesPattern("[0-9a-f]{64}")))
                .andReturn();
        final String soumissionId = champ(depot, "$.id");
        final String empreinte = champ(depot, "$.empreinte");

        // Une seule soumission par fournisseur et par besoin
        mockMvc.perform(post("/api/v1/soumissions").header("Authorization", frs[0]).contentType(MediaType.APPLICATION_JSON)
                        .content(offre.formatted(besoinId, frs[1], "44700", l1, ligne2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SOUMISSION_DEJA_DEPOSEE"));

        // Modification avant la date limite : nouveau sceau
        mockMvc.perform(put("/api/v1/soumissions/{id}", soumissionId).header("Authorization", frs[0])
                        .contentType(MediaType.APPLICATION_JSON).content(offre.formatted(besoinId, frs[1], "44000",
                                l1, ligne2).replace("3500", "3400")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montantProposeHT").value(36501.00))
                .andExpect(jsonPath("$.dateDerniereModification").isNotEmpty())
                .andExpect(jsonPath("$.empreinte").value(org.hamcrest.Matchers.not(empreinte)));

        // Le fournisseur ne voit que ses soumissions
        mockMvc.perform(get("/api/v1/soumissions").header("Authorization", frs[0]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].lignes").doesNotExist());
        mockMvc.perform(get("/api/v1/soumissions/{id}", soumissionId).header("Authorization", autre[0]))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/soumissions/{id}", soumissionId).header("Authorization", frs[0]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lignes[0].montantTotalLigneHT").value(34000.00));

        // Une soumission déposée fige le besoin et le RIB utilisé
        mockMvc.perform(put("/api/v1/besoins/{id}", besoinId).header("Authorization", ordo)
                        .contentType(MediaType.APPLICATION_JSON).content(corpsBesoin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BESOIN_NON_MODIFIABLE"));
        final String fournisseurId = jdbcTemplate.queryForObject(
                "SELECT fournisseur_id FROM soumissions WHERE id = ?", String.class, soumissionId);
        mockMvc.perform(delete("/api/v1/admin/fournisseurs/{f}/ribs/{r}", fournisseurId, frs[1]).header("Authorization", admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RIB_UTILISE"));
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
