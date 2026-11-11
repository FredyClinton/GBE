package gov.cmr.minfi.db.gbe.app.depense.controle;

import gov.cmr.minfi.db.gbe.app.depense.controle.dto.DecisionVisaRequest;
import gov.cmr.minfi.db.gbe.app.depense.controle.dto.VisaResponse;

import java.util.List;

public interface ControleCfService {

    /** UC06 « include » UC07, UC08 : une seule décision transactionnelle du CF sur un acte soumis (Q1). */
    VisaResponse decider(String acteId, DecisionVisaRequest request);

    /** Historique des visas d'un acte (rejets, réserves, visa favorable). */
    List<VisaResponse> getVisas(String acteId);
}
