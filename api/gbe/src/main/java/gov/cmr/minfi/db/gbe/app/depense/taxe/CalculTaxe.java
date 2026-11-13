package gov.cmr.minfi.db.gbe.app.depense.taxe;

import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;

import java.math.BigDecimal;

/**
 * Calcul des taxes d'une offre à partir de son total HT (décision Q23 : c'est le serveur qui calcule).
 * Point d'extension prévu (plan §4.4) : les méthodes dépendront du type de besoin — d'abord de sa nature
 * économique — et du régime fiscal du fournisseur ; elles seront ajoutées sous forme de stratégies.
 * Implémentation actuelle : {@link TvaEtIrParametres}.
 */
public interface CalculTaxe {

    /**
     * @param besoin    besoin auquel l'offre répond (nature économique…), pour les méthodes à venir
     * @param montantHT total HT calculé à partir des prix unitaires
     */
    MontantsTaxes calculer(ExpressionBesoin besoin, BigDecimal montantHT);
}
