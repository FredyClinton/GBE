package gov.cmr.minfi.db.gbe.app.depense.taxe;

import java.math.BigDecimal;

/**
 * Détail des montants d'une offre ou d'un acte, comme sur le BCA (plan §4.5) :
 * TTC = HT + TVA ; net à payer = TTC − TVA − IR (= HT − IR).
 */
public record MontantsTaxes(
        BigDecimal montantHT,
        BigDecimal tauxTva,
        BigDecimal montantTva,
        BigDecimal tauxIr,
        BigDecimal montantIr,
        BigDecimal montantTTC,
        BigDecimal montantNetAPayer
) {
}
