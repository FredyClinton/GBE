package gov.cmr.minfi.db.gbe.app.prediction;

import gov.cmr.minfi.db.gbe.app.prediction.dto.PredictionAERequest;
import gov.cmr.minfi.db.gbe.app.prediction.dto.PredictionAEResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Tag(name = "Prédictions", description = "Prédiction des profils d'engagement de crédits (module GBE+)")
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/ae")
    @ResponseStatus(HttpStatus.OK)
    public PredictionAEResponse predireProfilAE(@Valid @RequestBody PredictionAERequest requete) {
        return predictionService.predireProfilAE(requete);
    }
}
