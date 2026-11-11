package gov.cmr.minfi.db.gbe.app.referentiel.exercice;

import gov.cmr.minfi.db.gbe.app.referentiel.exercice.dto.ExerciceResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = OpenApiTags.REF_EXERCICES)
public class ExerciceController {

    private final ExerciceService exerciceService;

    @Operation(
        summary = "Lister les exercices",
        description = "Retourne tous les exercices budgétaires."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des exercices")
    })
    @GetMapping("/exercices")
    @ResponseStatus(HttpStatus.OK)
    public List<ExerciceResponse> getAllExercices() {
        return exerciceService.getAllExercices();
    }
}
