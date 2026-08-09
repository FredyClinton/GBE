package gov.cmr.minfi.db.gbe.app.prediction.client;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.prediction.dto.PredictionAERequest;
import gov.cmr.minfi.db.gbe.app.prediction.dto.PredictionAEResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PredictionClient {

    private final RestClient gbePlusRestClient;

    public PredictionAEResponse predireAE(PredictionAERequest requete) {
        final GbePlusPredictionRequest corpsRequete = new GbePlusPredictionRequest(
                requete.exercice(),
                requete.chapitreCode(),
                requete.rubriqueCode(),
                requete.dotationAE().doubleValue()
        );

        final GbePlusPredictionResponse reponse;
        try {
            reponse = gbePlusRestClient.post()
                    .uri("/predictions/ae")
                    .body(corpsRequete)
                    .retrieve()
                    .body(GbePlusPredictionResponse.class);
        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            // GBE+ renvoie 503 explicitement quand le modèle n'a pas pu être
            // chargé au démarrage (voir gbe_plus_api/main.py::lifespan).
            log.warn("GBE+ indisponible (modèle non chargé) : {}", ex.getMessage());
            throw new BusinessException(ErrorCode.PREDICTION_SERVICE_UNAVAILABLE);
        } catch (RestClientException ex) {
            log.error("Erreur d'appel à GBE+ : {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.PREDICTION_SERVICE_ERROR);
        }

        if (reponse == null) {
            throw new BusinessException(ErrorCode.PREDICTION_SERVICE_ERROR);
        }

        return PredictionAEResponse.builder()
                .exercice(reponse.exercice())
                .chapitreCode(reponse.chapitre())
                .rubriqueCode(reponse.rubrique())
                .profilPredit(reponse.profilPredit())
                .archetype(reponse.archetype())
                .niveauHistorique(reponse.niveauHistorique())
                .avertissement(reponse.avertissement())
                .montantsPredits(reponse.montantsPredits())
                .build();
    }
}
