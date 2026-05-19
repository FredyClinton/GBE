package gov.cmr.minfi.db.gbe.app.referentiel;

import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.exercice.ExerciceMapper;
import gov.cmr.minfi.db.gbe.app.exercice.ExerciceRepository;
import gov.cmr.minfi.db.gbe.app.exercice.ExerciceResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.ChapitreRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.ChapitreResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.SectionResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ProgrammeRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ProgrammeResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/referentiel")
@RequiredArgsConstructor
@Tag(name = "Référentiel", description = "Endpoints de consultation du référentiel budgétaire")
public class ReferentielController {

	private final ExerciceRepository exerciceRepository;
	private final ProgrammeRepository programmeRepository;
	private final ActionRepository actionRepository;
	private final SectionRepository sectionRepository;
	private final ChapitreRepository chapitreRepository;
	private final ExerciceMapper exerciceMapper;
	private final ReferentielMapper referentielMapper;

	// ── Exercices ────────────────────────────────────

	@GetMapping("/exercices")
	@ResponseStatus(HttpStatus.OK)
	public List<ExerciceResponse> getAllExercices() {
		return exerciceRepository.findAll().stream().map(exerciceMapper::toResponse).toList();
	}

	// ── Sections ─────────────────────────────────────

	@GetMapping("/sections")
	@ResponseStatus(HttpStatus.OK)
	public List<SectionResponse> getAllSections() {
		return sectionRepository.findAll().stream().map(referentielMapper::toSectionResponse).toList();
	}

	@GetMapping("/sections/exercice/{exerciceId}")
	@ResponseStatus(HttpStatus.OK)
	public List<SectionResponse> getSectionsByExercice(@PathVariable String exerciceId) {
		return sectionRepository.findByExerciceId(exerciceId).stream().map(referentielMapper::toSectionResponse).toList();
	}

	//  Programmes

	@GetMapping("/programmes")
	@ResponseStatus(HttpStatus.OK)
	public List<ProgrammeResponse> getAllProgrammes() {
		return programmeRepository.findAll().stream().map(referentielMapper::toProgrammeResponse).toList();
	}

	@GetMapping("/sections/{sectionId}/programmes")
	@ResponseStatus(HttpStatus.OK)
	public List<ProgrammeResponse> getProgrammesBySection(@PathVariable String sectionId) {
		return programmeRepository.findBySectionId(sectionId).stream().map(referentielMapper::toProgrammeResponse).toList();
	}

	@GetMapping("/exercice/{exerciceId}/programmes")
	@ResponseStatus(HttpStatus.OK)
	public List<ProgrammeResponse> getProgrammesByExercice(@PathVariable String exerciceId) {
		return programmeRepository.findByExerciceId(exerciceId).stream().map(referentielMapper::toProgrammeResponse).toList();
	}

	// ── Actions ───────────────────────────────────────

	@GetMapping("/programmes/{programmeId}/actions")
	@ResponseStatus(HttpStatus.OK)
	public List<ActionResponse> getActionsByProgramme(@PathVariable String programmeId) {
		return actionRepository.findByProgrammeId(programmeId).stream().map(referentielMapper::toActionResponse).toList();
	}

	@GetMapping("/exercice/{exerciceId}/programmes/{programmeId}/actions")
	@ResponseStatus(HttpStatus.OK)
	public List<ActionResponse> getActionsByProgrammeAndExercice(
			@PathVariable String exerciceId,
			@PathVariable String programmeId) {
		return actionRepository.findByProgrammeIdAndExerciceId(programmeId, exerciceId).stream()
				.map(referentielMapper::toActionResponse).toList();
	}

	// ── Chapitres ─────────────────────────────────────

	@GetMapping("/sections/{sectionId}/chapitres")
	@ResponseStatus(HttpStatus.OK)
	public List<ChapitreResponse> getChapitresBySection(@PathVariable String sectionId) {
		return chapitreRepository.findBySectionId(sectionId).stream().map(referentielMapper::toChapitreResponse).toList();
	}

	@GetMapping("/exercice/{exerciceId}/chapitres")
	@ResponseStatus(HttpStatus.OK)
	public List<ChapitreResponse> getChapitresByExercice(@PathVariable String exerciceId) {
		return chapitreRepository.findByExerciceId(exerciceId).stream().map(referentielMapper::toChapitreResponse).toList();
	}
}
