package gov.cmr.minfi.db.gbe.app.depense.taxe;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Méthode provisoire, conforme au modèle (montantProposeHT et montantProposeTTC, sans taux) :
 * le fournisseur saisit le TTC, qui doit être renseigné et au moins égal au HT.
 */
@Component
public class TtcSaisiParFournisseur implements CalculTaxe {

    @Override
    public BigDecimal calculerTTC(ExpressionBesoin besoin, BigDecimal montantHT, BigDecimal ttcSaisi) {
        if (ttcSaisi == null || ttcSaisi.compareTo(montantHT) < 0) {
            throw new BusinessException(ErrorCode.MONTANT_TTC_INVALIDE, montantHT);
        }
        return ttcSaisi;
    }
}
