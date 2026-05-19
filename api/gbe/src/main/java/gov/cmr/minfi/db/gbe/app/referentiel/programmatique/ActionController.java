package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/actions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = "Actions", description = "Gestion des actions budgétaires")
public class ActionController {

    private final ActionService actionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActionResponse createAction(@Valid @RequestBody ActionRequest request) {
        return actionService.createAction(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getAllActions() {
        return actionService.getAllActions();
    }

    @GetMapping("/{actionId}")
    @ResponseStatus(HttpStatus.OK)
    public ActionResponse getAction(@PathVariable String actionId) {
        return actionService.getAction(actionId);
    }

    @GetMapping("/programme/{programmeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getActionsByProgramme(@PathVariable String programmeId) {
        return actionService.getActionsByProgramme(programmeId);
    }

    @GetMapping("/programme/{programmeId}/exercice/{exerciceId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getActionsByProgrammeAndExercice(
            @PathVariable String programmeId,
            @PathVariable String exerciceId) {
        return actionService.getActionsByProgrammeAndExercice(programmeId, exerciceId);
    }

    @PutMapping("/{actionId}")
    @ResponseStatus(HttpStatus.OK)
    public ActionResponse updateAction(@PathVariable String actionId, @Valid @RequestBody ActionRequest request) {
        return actionService.updateAction(actionId, request);
    }

    @DeleteMapping("/{actionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAction(@PathVariable String actionId) {
        actionService.deleteAction(actionId);
    }
}
