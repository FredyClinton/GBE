package gov.cmr.minfi.db.gbe.app.depense.engagement.forme;

import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.engagement.ActeJuridique;
import gov.cmr.minfi.db.gbe.app.depense.engagement.TypeActe;
import gov.cmr.minfi.db.gbe.app.depense.soumission.Soumission;

import java.math.BigDecimal;

/**
 * Forme contractuelle de l'acte (« FormeActeStrategie » de l'onglet 4) : un bean Spring par {@link TypeActe}, qui
 * porte le comportement de la forme ; ses données sont dans une entité propre rattachée à l'acte (ex.
 * {@link BonDeCommande}). Seule {@link StrategieBonDeCommande} existe (Q21). La génération du document
 * (genererModele, imprimerBCA) et le circuit de visa propre à chaque forme viendront plus tard (plan §7).
 */
public interface FormeActeStrategie {

    TypeActe type();

    /** verifierEligibilite de l'onglet 4 : seuils (Q3), nature, durée. Table des seuils vide → toujours éligible. */
    boolean verifierEligibilite(ExpressionBesoin besoin, BigDecimal montantTTC);

    /** Clé du compteur des numéros d'ordre. */
    String cleCompteur(ExpressionBesoin besoin);

    /** Numéro de l'acte à partir de son numéro d'ordre. */
    String numeroter(ExpressionBesoin besoin, long numeroOrdre);

    /**
     * Crée ou met à jour les données de la forme sur l'acte, à partir de la saisie de l'ordonnateur et, pour les
     * valeurs proposées par défaut, de l'offre retenue.
     */
    void appliquerDonnees(ActeJuridique acte, Soumission retenue, DonneesForme donnees);
}
