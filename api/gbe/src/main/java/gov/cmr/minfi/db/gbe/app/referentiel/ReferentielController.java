package gov.cmr.minfi.db.gbe.app.referentiel;

import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.exercice.ExerciceRepository;
import gov.cmr.minfi.db.gbe.app.exercice.ExerciceResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.SectionResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Action;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
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

    @GetMapping("/exercices")
    @ResponseStatus(HttpStatus.OK)
    public List<ExerciceResponse> getAllExercices() {
        return exerciceRepository.findAll()
                .stream()
                .map(this::toExerciceResponse)
                .toList();
    }

    @GetMapping("/sections")
    @ResponseStatus(HttpStatus.OK)
    public List<SectionResponse> getAllSections() {
        return sectionRepository.findAll()
                .stream()
                .map(this::toSectionResponse)
                .toList();
    }

    @GetMapping("/sections/exercice/{exerciceId}")
    @ResponseStatus(HttpStatus.OK)
    public List<SectionResponse> getSectionsByExercice(
            @PathVariable String exerciceId
    ) {
        return sectionRepository.findByExerciceId(exerciceId)
                .stream()
                .map(this::toSectionResponse)
                .toList();
    }

    @GetMapping("/sections/{sectionId}/programmes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProgrammeResponse> getProgrammesBySection(
            @PathVariable String sectionId
    ) {
        return programmeRepository.findBySectionIdAndActifTrue(sectionId)
                .stream()
                .map(this::toProgrammeResponse)
                .toList();
    }

    @GetMapping("/programmes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProgrammeResponse> getAllProgrammes() {
        return programmeRepository.findAll()
                .stream()
                .map(this::toProgrammeResponse)
                .toList();
    }

    @GetMapping("/programmes/{programmeId}/actions")
    @ResponseStatus(HttpStatus.OK)
    public List<ActionResponse> getActionsByProgramme(
            @PathVariable String programmeId
    ) {
        return actionRepository.findByProgrammeId(programmeId)
                .stream()
                .map(this::toActionResponse)
                .toList();
    }

    private ExerciceResponse toExerciceResponse(Exercice exercice) {
        return ExerciceResponse.builder()
                .id(exercice.getId())
                .annee(exercice.getAnnee())
                .codeExercice(exercice.getCodeExercice())
                .libelleFr(exercice.getLibelleFr())
                .libelleEn(exercice.getLibelleEn())
                .actif(exercice.isActif())
                .build();
    }

    private SectionResponse toSectionResponse(Section section) {
        return SectionResponse.builder()
                .id(section.getId())
                .codeSection(section.getCodeSection())
                .sigle(section.getSigle())
                .libelleFr(section.getLibelleFr())
                .libelleEn(section.getLibelleEn())
                .typeSection(section.getTypeSection() != null
                        ? section.getTypeSection().name()
                        : null)
                .build();
    }

    private ProgrammeResponse toProgrammeResponse(Programme programme) {
        return ProgrammeResponse.builder()
                .id(programme.getId())
                .code(programme.getCode())
                .autreCode(programme.getAutreCode())
                .libelleFr(programme.getLibelleFr())
                .libelleEn(programme.getLibelleEn())
                .sectionId(programme.getSection().getId())
                .sectionLibelle(programme.getSection().getLibelleFr())
                .actif(programme.isActif())
                .build();
    }

    private ActionResponse toActionResponse(Action action) {
        return ActionResponse.builder()
                .id(action.getId())
                .codeAction(action.getCodeAction())
                .autreCode(action.getAutreCode())
                .libelleFr(action.getLibelleFr())
                .libelleEn(action.getLibelleEn())
                .programmeId(action.getProgramme().getId())
                .programmeLibelle(action.getProgramme().getLibelleFr())
                .build();
    }


}
