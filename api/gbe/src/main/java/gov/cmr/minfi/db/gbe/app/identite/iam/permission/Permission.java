package gov.cmr.minfi.db.gbe.app.identite.iam.permission;

public enum Permission {

    // ── Ordonnateur ───────────────────────────────────
    ENGAGE_DEPENSE("Engager une dépense"),
    REVISER_AE("Réviser les AE"),
    REVISER_CP("Réviser les CP"),
    REJETER_DEPENSE("Rejeter une dépense"),
    EXPRIMER_BESOIN("Exprimer un besoin"),
    ORDONNANCER("Ordonnancer une dépense"),

    // ── Contrôleur financier ──────────────────────────
    VISA_CFI("Viser une dépense"),
    REJETER_CFI("Rejeter au niveau CFI"),
    RESERVER_CREDIT("Réserver les AE sur une ligne de crédit"),
    QUITUS_NATURE("Donner le quitus sur la nature des matières"),
    VISER_ORDONNANCEMENT("Viser un ordonnancement"),

    // ── Comptable matière ─────────────────────────────
    RECEPTIONNER_LIVRAISON("Réceptionner une livraison"),

    // ── Fournisseur (externe) ─────────────────────────
    SOUMISSIONNER("Soumettre une offre"),

    // ── Comptable ─────────────────────────────────────
    LIQUIDER_DEPENSE("Liquider une dépense"),
    PAYER_DEPENSE("Payer une dépense"),

    // ── Admin ─────────────────────────────────────────
    INSCRIRE_CREDIT("Inscrire un crédit budgétaire au système"),
    MANAGE_USERS("Gérer les utilisateurs"),
    MANAGE_AFFECTATIONS("Gérer les mandats"),

    // ── Lecture seule (Gestionnaire) ──────────────────
    CONSULTER("Consulter les crédits et dépenses");

    private final String libelle;

    Permission(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
