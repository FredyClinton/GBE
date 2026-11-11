package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = OpenApiTags.REF_PROGRAMMES_ACTIONS)
public class ActionController {

    private final ActionService actionService;

    @Operation(
        summary = "Créer une action",
        description = "Crée une action rattachée à un programme."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Action créée"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActionResponse createAction(@Valid @RequestBody ActionRequest request) {
        return actionService.createAction(request);
    }

    @Operation(
        summary = "Lister les actions",
        description = "Retourne toutes les actions (administration)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des actions"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getAllActions() {
        return actionService.getAllActions();
    }

    @Operation(
        summary = "Consulter une action",
        description = "Retourne une action (administration)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Action trouvée"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Action introuvable")
    })
    @GetMapping("/{actionId}")
    @ResponseStatus(HttpStatus.OK)
    public ActionResponse getAction(@Parameter(description = "Identifiant de l'action", required = true) @PathVariable String actionId) {
        return actionService.getAction(actionId);
    }

    @Operation(
        summary = "Lister les actions d'un programme",
        description = "Retourne les actions du programme (administration)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des actions"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @GetMapping("/programme/{programmeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getActionsByProgramme(@Parameter(description = "Identifiant du programme", required = true) @PathVariable String programmeId) {
        return actionService.getActionsByProgramme(programmeId);
    }

    @Operation(
        summary = "Lister les actions d'un programme pour un exercice",
        description = "Retourne les actions du programme pour l'exercice (administration)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des actions"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @GetMapping("/programme/{programmeId}/exercice/{exerciceId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getActionsByProgrammeAndExercice(
            @Parameter(description = "Identifiant du programme", required = true) @PathVariable String programmeId,
            @Parameter(description = "Identifiant de l'exercice", required = true) @PathVariable String exerciceId) {
        return actionService.getActionsByProgrammeAndExercice(programmeId, exerciceId);
    }

    @Operation(
        summary = "Modifier une action",
        description = "Met à jour une action."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Action modifiée"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Action introuvable")
    })
    @PutMapping("/{actionId}")
    @ResponseStatus(HttpStatus.OK)
    public ActionResponse updateAction(@Parameter(description = "Identifiant de l'action", required = true) @PathVariable String actionId, @Valid @RequestBody ActionRequest request) {
        return actionService.updateAction(actionId, request);
    }

    @Operation(
        summary = "Supprimer une action",
        description = "Supprime une action qui n'a pas d'activité."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Action supprimée"),
        @ApiResponse(responseCode = "400", description = "L'action a des activités (ACTION_A_DES_ACTIVITES)"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Action introuvable")
    })
    @DeleteMapping("/{actionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAction(@Parameter(description = "Identifiant de l'action", required = true) @PathVariable String actionId) {
        actionService.deleteAction(actionId);
    }
}
