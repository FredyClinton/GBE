package gov.cmr.minfi.db.gbe.app.referentiel.economique;

import gov.cmr.minfi.db.gbe.app.referentiel.economique.dto.NatureEconomiqueResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.REF_NATURES_ECONOMIQUES)
public class NatureEconomiqueController {

    private final NatureEconomiqueService natureEconomiqueService;

    @Operation(
        summary = "Lister les natures économiques",
        description = "Retourne toutes les natures économiques."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des natures économiques")
    })
    @GetMapping("/natures-economiques")
    public ResponseEntity<List<NatureEconomiqueResponse>> getAllNaturesEconomiques() {
        return ResponseEntity.ok(natureEconomiqueService.getAllNaturesEconomiques());
    }

    @Operation(
        summary = "Lister les natures économiques d'un exercice",
        description = "Retourne les natures économiques rattachées à l'exercice. " +
            "Seules celles-ci sont acceptées pour un crédit ou un besoin de cet exercice."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des natures économiques")
    })
    @GetMapping("/exercice/{exerciceId}/natures-economiques")
    public ResponseEntity<List<NatureEconomiqueResponse>> getNaturesEconomiquesByExercice(@Parameter(description = "Identifiant de l'exercice", required = true) @PathVariable String exerciceId) {
        return ResponseEntity.ok(natureEconomiqueService.getNaturesEconomiquesByExercice(exerciceId));
    }
}
