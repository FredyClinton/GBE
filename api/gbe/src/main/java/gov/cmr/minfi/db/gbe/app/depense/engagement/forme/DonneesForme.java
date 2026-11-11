package gov.cmr.minfi.db.gbe.app.depense.engagement.forme;

/**
 * Données saisies par l'ordonnateur pour la forme de l'acte. Chaque forme n'en lit que ce qui la concerne ;
 * les champs des autres formes (LC, marché, bail) s'ajouteront ici quand elles seront traitées (plan §7).
 *
 * @param delaiLivraisonMois BCA : délai de livraison en mois (null à la création → proposé depuis l'offre, Q25)
 * @param referenceDevis     BCA : référence du devis (onglet 4), facultative
 */
public record DonneesForme(Integer delaiLivraisonMois, String referenceDevis) {
}
