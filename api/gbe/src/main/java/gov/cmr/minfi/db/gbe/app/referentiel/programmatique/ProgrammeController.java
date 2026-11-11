package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ProgrammeResponse;
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

/**
 * Consultation des programmes et de leurs actions.
 * La gestion (CRUD) des actions est exposée séparément par {@link ActionController}.
 */
@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.REF_PROGRAMMES_ACTIONS)
public class ProgrammeController {

    private final ProgrammeService programmeService;
    private final ActionService actionService;

    @Operation(
        summary = "Lister les programmes",
        description = "Retourne tous les programmes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des programmes")
    })
    @GetMapping("/programmes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProgrammeResponse> getAllProgrammes() {
        return programmeService.getAllProgrammes();
    }

    @Operation(
        summary = "Lister les programmes d'une section",
        description = "Retourne les programmes rattachés à la section."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des programmes")
    })
    @GetMapping("/sections/{sectionId}/programmes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProgrammeResponse> getProgrammesBySection(@Parameter(description = "Identifiant de la section", required = true) @PathVariable String sectionId) {
        return programmeService.getProgrammesBySection(sectionId);
    }

    @Operation(
        summary = "Lister les programmes d'un exercice",
        description = "Retourne les programmes de l'exercice."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des programmes")
    })
    @GetMapping("/exercice/{exerciceId}/programmes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProgrammeResponse> getProgrammesByExercice(@Parameter(description = "Identifiant de l'exercice", required = true) @PathVariable String exerciceId) {
        return programmeService.getProgrammesByExercice(exerciceId);
    }

    @Operation(
        summary = "Lister les actions d'un programme",
        description = "Retourne les actions rattachées au programme."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des actions")
    })
    @GetMapping("/programmes/{programmeId}/actions")
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getActionsByProgramme(@Parameter(description = "Identifiant du programme", required = true) @PathVariable String programmeId) {
        return actionService.getActionsByProgramme(programmeId);
    }

    @Operation(
        summary = "Lister les actions d'un programme pour un exercice",
        description = "Retourne les actions du programme pour l'exercice."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des actions")
    })
    @GetMapping("/exercice/{exerciceId}/programmes/{programmeId}/actions")
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getActionsByProgrammeAndExercice(
            @Parameter(description = "Identifiant de l'exercice", required = true) @PathVariable String exerciceId,
            @Parameter(description = "Identifiant du programme", required = true) @PathVariable String programmeId) {
        return actionService.getActionsByProgrammeAndExercice(programmeId, exerciceId);
    }
}
