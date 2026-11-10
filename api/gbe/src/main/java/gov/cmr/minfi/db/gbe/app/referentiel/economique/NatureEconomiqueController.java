package gov.cmr.minfi.db.gbe.app.referentiel.economique;

import gov.cmr.minfi.db.gbe.app.referentiel.economique.dto.NatureEconomiqueResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = "Référentiel", description = "Endpoints de consultation du référentiel budgétaire")
public class NatureEconomiqueController {

    private final NatureEconomiqueService natureEconomiqueService;

    @GetMapping("/natures-economiques")
    @ResponseStatus(HttpStatus.OK)
    public List<NatureEconomiqueResponse> getAllNaturesEconomiques() {
        return natureEconomiqueService.getAllNaturesEconomiques();
    }

    @GetMapping("/exercice/{exerciceId}/natures-economiques")
    @ResponseStatus(HttpStatus.OK)
    public List<NatureEconomiqueResponse> getNaturesEconomiquesByExercice(@PathVariable String exerciceId) {
        return natureEconomiqueService.getNaturesEconomiquesByExercice(exerciceId);
    }
}
