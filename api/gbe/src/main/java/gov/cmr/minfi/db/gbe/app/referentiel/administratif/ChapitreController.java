package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.ChapitreResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = "Référentiel", description = "Endpoints de consultation du référentiel budgétaire")
public class ChapitreController {

    private final ChapitreService chapitreService;

    @GetMapping("/sections/{sectionId}/chapitres")
    @ResponseStatus(HttpStatus.OK)
    public List<ChapitreResponse> getChapitresBySection(@PathVariable String sectionId) {
        return chapitreService.getChapitresBySection(sectionId);
    }

    @GetMapping("/exercice/{exerciceId}/chapitres")
    @ResponseStatus(HttpStatus.OK)
    public List<ChapitreResponse> getChapitresByExercice(@PathVariable String exerciceId) {
        return chapitreService.getChapitresByExercice(exerciceId);
    }
}
