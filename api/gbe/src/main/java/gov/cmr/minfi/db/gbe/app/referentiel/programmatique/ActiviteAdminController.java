package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Gestion des activites. Même permission que la gestion des actions. */
@RestController
@RequestMapping("/api/v1/admin/activites")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = OpenApiTags.REF_ACTIVITES_TACHES)
public class ActiviteAdminController {

	private final ActiviteService activiteService;

	@Operation(
		summary = "Créer une activité",
		description = "Crée une activité rattachée à une action, avec un code unique dans l'action. " +
			"Le programme, la section et l'exercice sont recopiés de l'action, jamais lus dans la requête."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Activité créée"),
		@ApiResponse(responseCode = "400", description = "Code déjà utilisé dans l'action ou données invalides"),
		@ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
		@ApiResponse(responseCode = "404", description = "Action introuvable")
	})
	@PostMapping
	public ResponseEntity<ActiviteResponse> createActivite(@Valid @RequestBody ActiviteRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(activiteService.createActivite(request));
	}

	@Operation(
		summary = "Modifier une activité",
		description = "Met à jour l'activité. " +
			"Un changement d'action recopie à nouveau le programme, la section et l'exercice, sur l'activité et sur ses tâches."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Activité modifiée"),
		@ApiResponse(responseCode = "400", description = "Code déjà utilisé dans l'action ou données invalides"),
		@ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
		@ApiResponse(responseCode = "404", description = "Activité ou action introuvable")
	})
	@PutMapping("/{activiteId}")
	public ResponseEntity<ActiviteResponse> updateActivite(@Parameter(description = "Identifiant de l'activité", required = true) @PathVariable String activiteId, @Valid @RequestBody ActiviteRequest request) {
		return ResponseEntity.ok(activiteService.updateActivite(activiteId, request));
	}

	@Operation(
		summary = "Supprimer une activité",
		description = "Supprime une activité qui n'a pas de tâche."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "Activité supprimée"),
		@ApiResponse(responseCode = "400", description = "L'activité a des tâches (ACTIVITE_A_DES_TACHES)"),
		@ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
		@ApiResponse(responseCode = "404", description = "Activité introuvable")
	})
	@DeleteMapping("/{activiteId}")
	public ResponseEntity<Void> deleteActivite(@Parameter(description = "Identifiant de l'activité", required = true) @PathVariable String activiteId) {
		activiteService.deleteActivite(activiteId);
		return ResponseEntity.noContent().build();
	}
}
