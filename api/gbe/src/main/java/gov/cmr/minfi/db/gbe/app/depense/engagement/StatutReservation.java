package gov.cmr.minfi.db.gbe.app.depense.engagement;

/** Statut de la réservation (« statut : String » de l'onglet 4, méthodes bloquer() / annuler()). */
public enum StatutReservation {
    /** AE consommées sur la ligne au profit de l'acte. */
    ACTIVE,
    /** Acte visé ou signé puis annulé : AE libérées (Q2, étape 2d). */
    ANNULEE
}
