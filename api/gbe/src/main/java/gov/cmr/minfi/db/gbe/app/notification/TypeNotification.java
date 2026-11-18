package gov.cmr.minfi.db.gbe.app.notification;

/** Événement à l'origine de la notification. La contrainte {@code notifications_type_check} liste ces valeurs. */
public enum TypeNotification {
    /** Acte signé mis à disposition du fournisseur (UC05). */
    ACTE_NOTIFIE,
    /** Acte annulé après sa notification au fournisseur. */
    ACTE_ANNULE,
    /** Visa, rejet ou réserves du CF, pour l'ordonnateur auteur de l'acte (UC06). */
    DECISION_CF
}
