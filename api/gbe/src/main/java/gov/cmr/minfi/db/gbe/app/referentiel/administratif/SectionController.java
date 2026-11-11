package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.SectionResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.REF_SECTIONS_CHAPITRES)
public class SectionController {

    private final SectionService sectionService;

    @Operation(
        summary = "Lister les sections",
        description = "Retourne toutes les sections."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des sections")
    })
    @GetMapping("/sections")
    @ResponseStatus(HttpStatus.OK)
    public List<SectionResponse> getAllSections() {
        return sectionService.getAllSections();
    }

    @Operation(
        summary = "Lister les sections d'un exercice",
        description = "Retourne les sections de l'exercice."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des sections")
    })
    @GetMapping("/sections/exercice/{exerciceId}")
    @ResponseStatus(HttpStatus.OK)
    public List<SectionResponse> getSectionsByExercice(@Parameter(description = "Identifiant de l'exercice", required = true) @PathVariable String exerciceId) {
        return sectionService.getSectionsByExercice(exerciceId);
    }
}
