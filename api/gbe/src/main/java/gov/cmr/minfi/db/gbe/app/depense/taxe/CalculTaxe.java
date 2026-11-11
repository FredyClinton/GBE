package gov.cmr.minfi.db.gbe.app.depense.taxe;

import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;

import java.math.BigDecimal;

/**
 * Calcul du montant TTC d'une offre. Point d'extension prévu (plan §4.4) : les méthodes de calcul des taxes
 * dépendront du type de besoin — d'abord de sa nature économique — et seront introduites plus tard sous forme
 * de stratégies. Une seule implémentation pour l'instant : {@link TtcSaisiParFournisseur}.
 */
public interface CalculTaxe {

    /**
     * @param besoin     besoin auquel l'offre répond (nature économique…)
     * @param montantHT  total HT calculé à partir des prix unitaires
     * @param ttcSaisi   TTC saisi par le fournisseur, éventuellement null selon la méthode
     * @return le TTC à retenir
     */
    BigDecimal calculerTTC(ExpressionBesoin besoin, BigDecimal montantHT, BigDecimal ttcSaisi);
}
