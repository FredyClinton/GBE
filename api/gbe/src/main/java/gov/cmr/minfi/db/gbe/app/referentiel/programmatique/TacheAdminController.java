package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheResponse;
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
@Tag(name = "Tâches", description = "Gestion des tâches budgétaires")
public class TacheAdminController {

	private final TacheService tacheService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TacheResponse createTache(@Valid @RequestBody TacheRequest request) {
		return tacheService.createTache(request);
	}

	@PutMapping("/{tacheId}")
	@ResponseStatus(HttpStatus.OK)
	public TacheResponse updateTache(@PathVariable String tacheId, @Valid @RequestBody TacheRequest request) {
		return tacheService.updateTache(tacheId, request);
	}

	@DeleteMapping("/{tacheId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTache(@PathVariable String tacheId) {
		tacheService.deleteTache(tacheId);
	}
}
