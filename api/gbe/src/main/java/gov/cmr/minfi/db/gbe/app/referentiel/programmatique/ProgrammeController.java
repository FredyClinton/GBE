package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ProgrammeResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consultation des programmes et de leurs actions.
 * La gestion (CRUD) des actions est exposée séparément par {@link ActionController}.
 */
@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = "Référentiel", description = "Endpoints de consultation du référentiel budgétaire")
public class ProgrammeController {

    private final ProgrammeService programmeService;
    private final ActionService actionService;

    @GetMapping("/programmes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProgrammeResponse> getAllProgrammes() {
        return programmeService.getAllProgrammes();
    }

    @GetMapping("/sections/{sectionId}/programmes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProgrammeResponse> getProgrammesBySection(@PathVariable String sectionId) {
        return programmeService.getProgrammesBySection(sectionId);
    }

    @GetMapping("/exercice/{exerciceId}/programmes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProgrammeResponse> getProgrammesByExercice(@PathVariable String exerciceId) {
        return programmeService.getProgrammesByExercice(exerciceId);
    }

    @GetMapping("/programmes/{programmeId}/actions")
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getActionsByProgramme(@PathVariable String programmeId) {
        return actionService.getActionsByProgramme(programmeId);
    }

    @GetMapping("/exercice/{exerciceId}/programmes/{programmeId}/actions")
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getActionsByProgrammeAndExercice(
            @PathVariable String exerciceId,
            @PathVariable String programmeId) {
        return actionService.getActionsByProgrammeAndExercice(programmeId, exerciceId);
    }
}
