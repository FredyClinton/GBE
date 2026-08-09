package gov.cmr.minfi.db.gbe.app.prediction.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Miroir exact de RequetePredictionAE (gbe_plus_api/schemas.py).
 * Reste interne au package client : PredictionController n'expose que les
 * DTO camelCase de gov.cmr.minfi.db.gbe.app.prediction.dto.
 */
record GbePlusPredictionRequest(
        int exercice,
        String chapitre,
        String rubrique,
        @JsonProperty("dotation_ae") double dotationAe
) {}
