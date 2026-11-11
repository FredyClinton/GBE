package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.ChapitreResponse;
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
@Tag(name = OpenApiTags.REF_SECTIONS_CHAPITRES)
public class ChapitreController {

    private final ChapitreService chapitreService;

    @Operation(
        summary = "Lister les chapitres d'une section",
        description = "Retourne les chapitres (structures émettrices) de la section."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des chapitres")
    })
    @GetMapping("/sections/{sectionId}/chapitres")
    public ResponseEntity<List<ChapitreResponse>> getChapitresBySection(@Parameter(description = "Identifiant de la section", required = true) @PathVariable String sectionId) {
        return ResponseEntity.ok(chapitreService.getChapitresBySection(sectionId));
    }

    @Operation(
        summary = "Lister les chapitres d'un exercice",
        description = "Retourne les chapitres de l'exercice."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des chapitres")
    })
    @GetMapping("/exercice/{exerciceId}/chapitres")
    public ResponseEntity<List<ChapitreResponse>> getChapitresByExercice(@Parameter(description = "Identifiant de l'exercice", required = true) @PathVariable String exerciceId) {
        return ResponseEntity.ok(chapitreService.getChapitresByExercice(exerciceId));
    }
}
