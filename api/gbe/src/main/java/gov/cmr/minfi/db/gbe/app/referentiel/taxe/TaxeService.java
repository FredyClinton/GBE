package gov.cmr.minfi.db.gbe.app.referentiel.taxe;

import gov.cmr.minfi.db.gbe.app.referentiel.taxe.dto.TauxTaxeRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.dto.TaxeResponse;

import java.util.List;

public interface TaxeService {

    List<TaxeResponse> getTaxes();

    TaxeResponse changerTaux(CodeTaxe code, TauxTaxeRequest request);
}
