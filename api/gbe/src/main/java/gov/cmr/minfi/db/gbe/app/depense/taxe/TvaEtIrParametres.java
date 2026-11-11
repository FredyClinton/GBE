package gov.cmr.minfi.db.gbe.app.depense.taxe;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.CodeTaxe;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.Taxe;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.TaxeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Méthode du BCA (plan §4.5) avec les taux de la table {@code taxes} : TVA et IR appliqués au HT et arrondis au centime ;
 * TTC = HT + TVA ; net à payer = TTC − TVA − IR. Mêmes taux pour tous les besoins et tous les fournisseurs pour l'instant.
 */
@Component
@RequiredArgsConstructor
public class TvaEtIrParametres implements CalculTaxe {

    private final TaxeRepository taxeRepository;

    @Override
    public MontantsTaxes calculer(ExpressionBesoin besoin, BigDecimal montantHT) {
        final Taxe tva = taxe(CodeTaxe.TVA);
        final Taxe ir = taxe(CodeTaxe.IR);
        final BigDecimal montantTva = tva.appliquer(montantHT);
        final BigDecimal montantIr = ir.appliquer(montantHT);
        final BigDecimal ttc = montantHT.add(montantTva);
        return new MontantsTaxes(montantHT, tva.getTaux(), montantTva, ir.getTaux(), montantIr,
                ttc, ttc.subtract(montantTva).subtract(montantIr));
    }

    private Taxe taxe(CodeTaxe code) {
        return taxeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAXE_NON_PARAMETREE, code));
    }
}
