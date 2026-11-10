package gov.cmr.minfi.db.gbe.app.referentiel.exercice;

import gov.cmr.minfi.db.gbe.app.referentiel.exercice.dto.ExerciceResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = "Référentiel", description = "Endpoints de consultation du référentiel budgétaire")
public class ExerciceController {

    private final ExerciceService exerciceService;

    @GetMapping("/exercices")
    @ResponseStatus(HttpStatus.OK)
    public List<ExerciceResponse> getAllExercices() {
        return exerciceService.getAllExercices();
    }
}
