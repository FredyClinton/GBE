package gov.cmr.minfi.db.gbe.app.common.config;

/**
 * Noms des groupes (tags) de la documentation Swagger, partagés par les contrôleurs.
 * L'ordre d'affichage et les descriptions sont déclarés une seule fois dans {@link OpenApiConfig}.
 * Tous les contrôleurs du référentiel (consultation et administration) portent un tag « Référentiel · … ».
 */
public final class OpenApiTags {

    public static final String AUTHENTIFICATION = "Authentification";
    public static final String MON_COMPTE = "Mon compte";

    public static final String ADMIN_UTILISATEURS = "Administration · Utilisateurs";
    public static final String ADMIN_AGENTS = "Administration · Agents";
    public static final String ADMIN_MANDATS = "Administration · Mandats";
    public static final String ADMIN_FOURNISSEURS = "Administration · Fournisseurs";

    public static final String REF_EXERCICES = "Référentiel · Exercices";
    public static final String REF_SECTIONS_CHAPITRES = "Référentiel · Sections et chapitres";
    public static final String REF_PROGRAMMES_ACTIONS = "Référentiel · Programmes et actions";
    public static final String REF_ACTIVITES_TACHES = "Référentiel · Activités et tâches";
    public static final String REF_NATURES_ECONOMIQUES = "Référentiel · Natures économiques";
    public static final String REF_MATIERES = "Référentiel · Matières";
    public static final String REF_TAXES = "Référentiel · Taxes";

    public static final String BUDGET_CREDITS = "Budget · Crédits budgétaires";

    public static final String DEPENSE_BESOINS = "Dépense · Expressions de besoin";
    public static final String DEPENSE_BESOINS_OUVERTS = "Dépense · Besoins ouverts (public)";
    public static final String DEPENSE_SOUMISSIONS = "Dépense · Soumissions";

    public static final String ESPACE_FOURNISSEUR = "Espace fournisseur";

    private OpenApiTags() {
    }
}
