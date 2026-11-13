package gov.cmr.minfi.db.gbe.app.common.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "DGB",
                        email = "dge.cellule.informatique"
                ),
                description = "API de la Gestion Budgétaire Électronique (MINFI). Les routes sont groupées par domaine : "
                        + "authentification, administration, référentiel, budget, dépense.",
                version = "1.0",
                license = @License(
                        name = "Proprietaire"
                ),
                termsOfService = "ras"

        ),
        servers = {
                @Server(
                        url = "https://gbe-8clf.onrender.com",
                        description = "Production server"
                ),
                @Server(
                        url = "http://localhost:8080",
                        description = "Development server"
                )
        },
        tags = {
                @Tag(name = OpenApiTags.AUTHENTIFICATION, description = "Connexion par mot de passe, double authentification TOTP et renouvellement des jetons. Routes publiques."),
                @Tag(name = OpenApiTags.MON_COMPTE, description = "Gestion de son propre compte par l'utilisateur connecté."),
                @Tag(name = OpenApiTags.ADMIN_UTILISATEURS, description = "Comptes des agents internes et rôles disponibles (`MANAGE_USERS`). Les fournisseurs se créent dans « Administration · Fournisseurs »."),
                @Tag(name = OpenApiTags.ADMIN_AGENTS, description = "Agents de l'administration, rattachés ou non à un compte (`MANAGE_USERS`)."),
                @Tag(name = OpenApiTags.ADMIN_MANDATS, description = "Mandats d'un utilisateur : section, programme facultatif, rôle et permissions (`MANAGE_AFFECTATIONS`)."),
                @Tag(name = OpenApiTags.ADMIN_FOURNISSEURS, description = "Fournisseurs (compte `ROLE_FOURNISSEUR` sans mandat) et leurs RIB (`MANAGE_USERS`)."),
                @Tag(name = OpenApiTags.REF_EXERCICES, description = "Exercices budgétaires."),
                @Tag(name = OpenApiTags.REF_SECTIONS_CHAPITRES, description = "Structure administrative : sections et chapitres."),
                @Tag(name = OpenApiTags.REF_PROGRAMMES_ACTIONS, description = "Programmes et actions. Consultation sous `/referentiel`, gestion des actions sous `/admin/actions` (`MANAGE_USERS`)."),
                @Tag(name = OpenApiTags.REF_ACTIVITES_TACHES, description = "Activités et tâches (Programme → Action → Activité → Tâche). Consultation sous `/referentiel`, gestion sous `/admin/activites` et `/admin/taches` (`MANAGE_USERS`)."),
                @Tag(name = OpenApiTags.REF_NATURES_ECONOMIQUES, description = "Natures économiques de la dépense."),
                @Tag(name = OpenApiTags.REF_MATIERES, description = "Catalogue des matières (biens et services). Consultation sous `/referentiel/matieres`, gestion sous `/admin/matieres` (`MANAGE_USERS`)."),
                @Tag(name = OpenApiTags.REF_TAXES, description = "Taux de TVA et d'IR appliqués par le serveur aux offres et aux actes. Consultation sous `/referentiel/taxes`, modification sous `/admin/taxes` (`MANAGE_USERS`)."),
                @Tag(name = OpenApiTags.BUDGET_CREDITS, description = "Lignes de crédit (AE/CP) : inscription, consultation dans le périmètre du mandat, révision et changements de statut."),
                @Tag(name = OpenApiTags.DEPENSE_BESOINS, description = "Expression, publication et suivi des besoins par l'ordonnateur (UC01), dans le périmètre de ses mandats."),
                @Tag(name = OpenApiTags.DEPENSE_BESOINS_OUVERTS, description = "Consultation sans compte des besoins ouverts aux soumissions (UC02)."),
                @Tag(name = OpenApiTags.DEPENSE_SOUMISSIONS, description = "Dépôt et suivi de ses offres par le fournisseur connecté (UC03, `SOUMISSIONNER`)."),
                @Tag(name = OpenApiTags.DEPENSE_ACTES, description = "Projet d'acte juridique préparé par l'ordonnateur à partir de l'offre retenue (UC04), seule la forme BCA pour l'instant ; contrôle du CF et signature à venir."),
                @Tag(name = OpenApiTags.DEPENSE_CONTROLE_CF, description = "Décision du contrôleur financier sur l'acte soumis (UC06, incluant la réservation des crédits UC07 et le quitus UC08) : visa favorable, rejet ou réserves."),
                @Tag(name = OpenApiTags.ESPACE_FOURNISSEUR, description = "Routes du fournisseur connecté (`ROLE_FOURNISSEUR`).")
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }

)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
