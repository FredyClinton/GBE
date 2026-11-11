package gov.cmr.minfi.db.gbe.app.depense.engagement.forme;

import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.engagement.TypeActe;

import java.math.BigDecimal;

/**
 * Forme contractuelle de l'acte (« FormeActeStrategie » de l'onglet 4) : un bean Spring par {@link TypeActe}.
 * Seule {@link StrategieBonDeCommande} existe (Q21). La génération du document (genererModele, imprimerBCA) et le
 * circuit de visa propre à chaque forme viendront plus tard (plan §7).
 */
public interface FormeActeStrategie {

    TypeActe type();

    /** verifierEligibilite de l'onglet 4 : seuils (Q3), nature, durée. Table des seuils vide → toujours éligible. */
    boolean verifierEligibilite(ExpressionBesoin besoin, BigDecimal montantTTC);

    /** Clé du compteur des numéros d'ordre. */
    String cleCompteur(ExpressionBesoin besoin);

    /** Numéro de l'acte à partir de son numéro d'ordre. */
    String numeroter(ExpressionBesoin besoin, long numeroOrdre);
}
