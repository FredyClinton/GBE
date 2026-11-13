package gov.cmr.minfi.db.gbe.app.referentiel.taxe.dto;

import gov.cmr.minfi.db.gbe.app.referentiel.taxe.CodeTaxe;

import java.math.BigDecimal;

public record TaxeResponse(CodeTaxe code, String libelle, BigDecimal taux) {
}
