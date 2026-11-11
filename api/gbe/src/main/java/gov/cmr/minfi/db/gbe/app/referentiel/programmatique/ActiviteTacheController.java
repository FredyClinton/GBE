package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Consultation des activités et des tâches (tout utilisateur authentifié). */
@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = "Référentiel", description = "Endpoints de consultation du référentiel budgétaire")
public class ActiviteTacheController {

	private final ActiviteService activiteService;
	private final TacheService tacheService;

	@GetMapping("/actions/{actionId}/activites")
	@ResponseStatus(HttpStatus.OK)
	public List<ActiviteResponse> getActivitesByAction(@PathVariable String actionId) {
		return activiteService.getActivitesByAction(actionId);
	}

	@GetMapping("/programmes/{programmeId}/activites")
	@ResponseStatus(HttpStatus.OK)
	public List<ActiviteResponse> getActivitesByProgramme(
			@PathVariable String programmeId,
			@RequestParam(required = false) String exerciceId) {
		return activiteService.getActivitesByProgramme(programmeId, exerciceId);
	}

	@GetMapping("/activites/{activiteId}")
	@ResponseStatus(HttpStatus.OK)
	public ActiviteResponse getActivite(@PathVariable String activiteId) {
		return activiteService.getActivite(activiteId);
	}

	@GetMapping("/activites/{activiteId}/taches")
	@ResponseStatus(HttpStatus.OK)
	public List<TacheResponse> getTachesByActivite(@PathVariable String activiteId) {
		return tacheService.getTachesByActivite(activiteId);
	}

	@GetMapping("/programmes/{programmeId}/taches")
	@ResponseStatus(HttpStatus.OK)
	public List<TacheResponse> getTachesByProgramme(
			@PathVariable String programmeId,
			@RequestParam(required = false) String exerciceId) {
		return tacheService.getTachesByProgramme(programmeId, exerciceId);
	}

	@GetMapping("/taches/{tacheId}")
	@ResponseStatus(HttpStatus.OK)
	public TacheResponse getTache(@PathVariable String tacheId) {
		return tacheService.getTache(tacheId);
	}
}
