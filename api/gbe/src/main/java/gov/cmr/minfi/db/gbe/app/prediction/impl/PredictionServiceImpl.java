package gov.cmr.minfi.db.gbe.app.prediction.impl;

import gov.cmr.minfi.db.gbe.app.prediction.PredictionService;
import gov.cmr.minfi.db.gbe.app.prediction.client.PredictionClient;
import gov.cmr.minfi.db.gbe.app.prediction.dto.PredictionAERequest;
import gov.cmr.minfi.db.gbe.app.prediction.dto.PredictionAEResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final PredictionClient predictionClient;

    @Override
    public PredictionAEResponse predireProfilAE(PredictionAERequest requete) {
        // Socle minimal (10-12/08) : la requête est transmise telle quelle à
        // GBE+, sans validation préalable de chapitreCode/rubriqueCode contre
        // les référentiels Section/NatureEconomique.
        // Bloc optionnel (enrichissement) : résoudre et valider ces codes
        // avant l'appel, journaliser la prédiction (table bdg_predictions).
        return predictionClient.predireAE(requete);
    }
}
