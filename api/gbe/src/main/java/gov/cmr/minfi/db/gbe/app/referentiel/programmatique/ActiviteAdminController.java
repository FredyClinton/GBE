package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Gestion des activites. Même permission que la gestion des actions. */
@RestController
@RequestMapping("/api/v1/admin/activites")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = "Activites", description = "Gestion des activites budgétaires")
public class ActiviteAdminController {

	private final ActiviteService activiteService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ActiviteResponse createActivite(@Valid @RequestBody ActiviteRequest request) {
		return activiteService.createActivite(request);
	}

	@PutMapping("/{activiteId}")
	@ResponseStatus(HttpStatus.OK)
	public ActiviteResponse updateActivite(@PathVariable String activiteId, @Valid @RequestBody ActiviteRequest request) {
		return activiteService.updateActivite(activiteId, request);
	}

	@DeleteMapping("/{activiteId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteActivite(@PathVariable String activiteId) {
		activiteService.deleteActivite(activiteId);
	}
}
