package gov.cmr.minfi.db.gbe.app.budget.depense.controle;

/**
 * Le CF a rendu sa décision sur un acte (notifierDecision() de l'onglet 4). Publié dans la transaction du visa.
 *
 * @param ordonnateurUserId auteur du projet d'acte ({@code createdBy}), à prévenir
 */
public record DecisionCfEvent(String acteId, String numero, StatutVisa decision, String observations,
                              String ordonnateurUserId) {
}
