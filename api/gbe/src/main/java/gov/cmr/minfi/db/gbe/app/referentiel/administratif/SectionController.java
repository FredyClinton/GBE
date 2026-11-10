package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.SectionResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = "Référentiel", description = "Endpoints de consultation du référentiel budgétaire")
public class SectionController {

    private final SectionService sectionService;

    @GetMapping("/sections")
    @ResponseStatus(HttpStatus.OK)
    public List<SectionResponse> getAllSections() {
        return sectionService.getAllSections();
    }

    @GetMapping("/sections/exercice/{exerciceId}")
    @ResponseStatus(HttpStatus.OK)
    public List<SectionResponse> getSectionsByExercice(@PathVariable String exerciceId) {
        return sectionService.getSectionsByExercice(exerciceId);
    }
}
