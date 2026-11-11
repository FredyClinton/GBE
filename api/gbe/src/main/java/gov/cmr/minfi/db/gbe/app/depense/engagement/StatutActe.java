package gov.cmr.minfi.db.gbe.app.depense.engagement;

/**
 * Cycle de vie de l'acte (énumération StatutActe du modèle). Décision Q18 : un rejet du CF renvoie l'acte en
 * PROJET (corrigeable, onglets 2 et 3) ; REJETE n'est pas utilisé. ANNULE : acte visé ou signé annulé (2d).
 */
public enum StatutActe {
    PROJET,
    SOUMIS_CONTROLE,
    VISE_CF,
    SIGNE_ORDONNATEUR,
    NOTIFIE_PRESTATAIRE,
    REJETE,
    ANNULE
}
