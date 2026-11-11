package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheResponse;
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

/** Gestion des tâches. Même permission que la gestion des actions. */
@RestController
@RequestMapping("/api/v1/admin/taches")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = OpenApiTags.REF_ACTIVITES_TACHES)
public class TacheAdminController {

	private final TacheService tacheService;

	@Operation(
		summary = "Créer une tâche",
		description = "Crée une tâche rattachée à une activité, avec un code unique dans l'activité. " +
			"L'action, le programme, la section et l'exercice sont recopiés de l'activité."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Tâche créée"),
		@ApiResponse(responseCode = "400", description = "Code déjà utilisé dans l'activité ou données invalides"),
		@ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
		@ApiResponse(responseCode = "404", description = "Activité introuvable")
	})
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TacheResponse createTache(@Valid @RequestBody TacheRequest request) {
		return tacheService.createTache(request);
	}

	@Operation(
		summary = "Modifier une tâche",
		description = "Met à jour la tâche ; un changement d'activité recopie à nouveau ses rattachements."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Tâche modifiée"),
		@ApiResponse(responseCode = "400", description = "Code déjà utilisé dans l'activité ou données invalides"),
		@ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
		@ApiResponse(responseCode = "404", description = "Tâche ou activité introuvable")
	})
	@PutMapping("/{tacheId}")
	@ResponseStatus(HttpStatus.OK)
	public TacheResponse updateTache(@Parameter(description = "Identifiant de la tâche", required = true) @PathVariable String tacheId, @Valid @RequestBody TacheRequest request) {
		return tacheService.updateTache(tacheId, request);
	}

	@Operation(
		summary = "Supprimer une tâche",
		description = "Supprime une tâche."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "Tâche supprimée"),
		@ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
		@ApiResponse(responseCode = "404", description = "Tâche introuvable")
	})
	@DeleteMapping("/{tacheId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTache(@Parameter(description = "Identifiant de la tâche", required = true) @PathVariable String tacheId) {
		tacheService.deleteTache(tacheId);
	}
}
