package gov.cmr.minfi.db.gbe.app.prediction;

import gov.cmr.minfi.db.gbe.app.prediction.dto.PredictionAERequest;
import gov.cmr.minfi.db.gbe.app.prediction.dto.PredictionAEResponse;

public interface PredictionService {
    PredictionAEResponse predireProfilAE(PredictionAERequest requete);
}
