package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheResponse;
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

/** Consultation des activités et des tâches (tout utilisateur authentifié). */
@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.REF_ACTIVITES_TACHES)
public class ActiviteTacheController {

	private final ActiviteService activiteService;
	private final TacheService tacheService;

	@Operation(
		summary = "Lister les activités d'une action",
		description = "Retourne les activités rattachées à l'action."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Liste des activités")
	})
	@GetMapping("/actions/{actionId}/activites")
	@ResponseStatus(HttpStatus.OK)
	public List<ActiviteResponse> getActivitesByAction(@Parameter(description = "Identifiant de l'action", required = true) @PathVariable String actionId) {
		return activiteService.getActivitesByAction(actionId);
	}

	@Operation(
		summary = "Lister les activités d'un programme",
		description = "Retourne les activités du programme, éventuellement limitées à un exercice."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Liste des activités")
	})
	@GetMapping("/programmes/{programmeId}/activites")
	@ResponseStatus(HttpStatus.OK)
	public List<ActiviteResponse> getActivitesByProgramme(
			@Parameter(description = "Identifiant du programme", required = true) @PathVariable String programmeId,
			@Parameter(description = "Filtre facultatif : identifiant de l'exercice") @RequestParam(required = false) String exerciceId) {
		return activiteService.getActivitesByProgramme(programmeId, exerciceId);
	}

	@Operation(
		summary = "Consulter une activité",
		description = "Retourne une activité."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Activité trouvée"),
		@ApiResponse(responseCode = "404", description = "Activité introuvable")
	})
	@GetMapping("/activites/{activiteId}")
	@ResponseStatus(HttpStatus.OK)
	public ActiviteResponse getActivite(@Parameter(description = "Identifiant de l'activité", required = true) @PathVariable String activiteId) {
		return activiteService.getActivite(activiteId);
	}

	@Operation(
		summary = "Lister les tâches d'une activité",
		description = "Retourne les tâches rattachées à l'activité."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Liste des tâches")
	})
	@GetMapping("/activites/{activiteId}/taches")
	@ResponseStatus(HttpStatus.OK)
	public List<TacheResponse> getTachesByActivite(@Parameter(description = "Identifiant de l'activité", required = true) @PathVariable String activiteId) {
		return tacheService.getTachesByActivite(activiteId);
	}

	@Operation(
		summary = "Lister les tâches d'un programme",
		description = "Retourne les tâches du programme, éventuellement limitées à un exercice. " +
			"Sert notamment à choisir la tâche d'une expression de besoin."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Liste des tâches")
	})
	@GetMapping("/programmes/{programmeId}/taches")
	@ResponseStatus(HttpStatus.OK)
	public List<TacheResponse> getTachesByProgramme(
			@Parameter(description = "Identifiant du programme", required = true) @PathVariable String programmeId,
			@Parameter(description = "Filtre facultatif : identifiant de l'exercice") @RequestParam(required = false) String exerciceId) {
		return tacheService.getTachesByProgramme(programmeId, exerciceId);
	}

	@Operation(
		summary = "Consulter une tâche",
		description = "Retourne une tâche."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Tâche trouvée"),
		@ApiResponse(responseCode = "404", description = "Tâche introuvable")
	})
	@GetMapping("/taches/{tacheId}")
	@ResponseStatus(HttpStatus.OK)
	public TacheResponse getTache(@Parameter(description = "Identifiant de la tâche", required = true) @PathVariable String tacheId) {
		return tacheService.getTache(tacheId);
	}
}
