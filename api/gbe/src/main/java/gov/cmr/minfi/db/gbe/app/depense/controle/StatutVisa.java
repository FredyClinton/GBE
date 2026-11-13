package gov.cmr.minfi.db.gbe.app.depense.controle;

/**
 * Décision du CF (énumération StatutVisa du modèle), sens fixé par Q18 :
 * FAVORABLE → acte VISE_CF ; DEFAVORABLE_REJET → acte renvoyé en PROJET ; SUSPENDU_RESERVES → acte en attente.
 */
public enum StatutVisa {
    FAVORABLE,
    DEFAVORABLE_REJET,
    SUSPENDU_RESERVES
}
