package gov.cmr.minfi.db.gbe.app.depense.engagement;

/**
 * Formes de l'acte juridique (énumération TypeActe du modèle). Seule la forme BCA est mise en œuvre
 * (décision Q21) ; les autres viendront quand les informations seront disponibles (plan §7).
 */
public enum TypeActe {
    BON_DE_COMMANDE,
    LETTRE_COMMANDE,
    MARCHE_PUBLIC,
    CONVENTION_BAIL
}
