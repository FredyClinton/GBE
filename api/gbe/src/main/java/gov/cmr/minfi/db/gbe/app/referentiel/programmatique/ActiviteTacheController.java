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
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<List<ActiviteResponse>> getActivitesByAction(@Parameter(description = "Identifiant de l'action", required = true) @PathVariable String actionId) {
		return ResponseEntity.ok(activiteService.getActivitesByAction(actionId));
	}

	@Operation(
		summary = "Lister les activités d'un programme",
		description = "Retourne les activités du programme, éventuellement limitées à un exercice."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Liste des activités")
	})
	@GetMapping("/programmes/{programmeId}/activites")
	public ResponseEntity<List<ActiviteResponse>> getActivitesByProgramme(
			@Parameter(description = "Identifiant du programme", required = true) @PathVariable String programmeId,
			@Parameter(description = "Filtre facultatif : identifiant de l'exercice") @RequestParam(required = false) String exerciceId) {
		return ResponseEntity.ok(activiteService.getActivitesByProgramme(programmeId, exerciceId));
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
	public ResponseEntity<ActiviteResponse> getActivite(@Parameter(description = "Identifiant de l'activité", required = true) @PathVariable String activiteId) {
		return ResponseEntity.ok(activiteService.getActivite(activiteId));
	}

	@Operation(
		summary = "Lister les tâches d'une activité",
		description = "Retourne les tâches rattachées à l'activité."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Liste des tâches")
	})
	@GetMapping("/activites/{activiteId}/taches")
	public ResponseEntity<List<TacheResponse>> getTachesByActivite(@Parameter(description = "Identifiant de l'activité", required = true) @PathVariable String activiteId) {
		return ResponseEntity.ok(tacheService.getTachesByActivite(activiteId));
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
	public ResponseEntity<List<TacheResponse>> getTachesByProgramme(
			@Parameter(description = "Identifiant du programme", required = true) @PathVariable String programmeId,
			@Parameter(description = "Filtre facultatif : identifiant de l'exercice") @RequestParam(required = false) String exerciceId) {
		return ResponseEntity.ok(tacheService.getTachesByProgramme(programmeId, exerciceId));
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
	public ResponseEntity<TacheResponse> getTache(@Parameter(description = "Identifiant de la tâche", required = true) @PathVariable String tacheId) {
		return ResponseEntity.ok(tacheService.getTache(tacheId));
	}
}
