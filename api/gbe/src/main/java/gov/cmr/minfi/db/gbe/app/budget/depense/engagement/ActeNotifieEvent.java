package gov.cmr.minfi.db.gbe.app.budget.depense.engagement;

/**
 * L'acte signé est notifié au fournisseur (UC05, Q20). Publié dans la transaction de la signature ; porte ce qu'il faut
 * pour prévenir le destinataire sans recharger l'acte.
 *
 * @param fournisseurUserId compte utilisateur du fournisseur titulaire
 */
public record ActeNotifieEvent(String acteId, String numero, String objet, String fournisseurUserId) {
}
