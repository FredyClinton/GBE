package gov.cmr.minfi.db.gbe.app.budget.depense.engagement;

/**
 * Un acte visé, signé ou notifié est annulé (Q2). Publié dans la transaction de l'annulation.
 *
 * @param fournisseurUserId compte du fournisseur s'il avait reçu l'acte, sinon {@code null}
 */
public record ActeAnnuleEvent(String acteId, String numero, String motif, String fournisseurUserId) {
}
