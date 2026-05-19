package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.exercice.ExerciceRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.ReferentielMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Action;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActionService;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ProgrammeRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActionServiceImpl implements ActionService {

    private final ActionRepository actionRepository;
    private final ProgrammeRepository programmeRepository;
    private final SectionRepository sectionRepository;
    private final ExerciceRepository exerciceRepository;
    private final ReferentielMapper referentielMapper;

    @Override
    @Transactional
    public ActionResponse createAction(ActionRequest request) {
        final Programme programme = findProgramme(request.programmeId());
        if (actionRepository.existsByCodeActionAndProgrammeId(request.codeAction(), request.programmeId())) {
            throw new BusinessException(ErrorCode.ACTION_ALREADY_EXISTS, request.codeAction());
        }
        final Section section = findSection(request.sectionId());
        final Exercice exercice = findExercice(request.exerciceId());

        final Action action = Action.builder()
                .codeAction(request.codeAction())
                .libelleFr(request.libelleFr())
                .libelleEn(request.libelleEn())
                .numero(request.numero())
                .codeProgramme(programme.getCode())
                .codeSection(section.getCodeSection())
                .codeExercice(String.valueOf(exercice.getCodeExercice()))
                .numeroProgramme(programme.getNumero())
                .programme(programme)
                .section(section)
                .exercice(exercice)
                .build();

        actionRepository.save(action);
        log.info("Created action {} in programme {}", action.getCodeAction(), programme.getCode());
        return referentielMapper.toActionResponse(action);
    }

    @Override
    public ActionResponse getAction(String actionId) {
        return referentielMapper.toActionResponse(findAction(actionId));
    }

    @Override
    public List<ActionResponse> getAllActions() {
        return actionRepository.findAll().stream().map(referentielMapper::toActionResponse).toList();
    }

    @Override
    public List<ActionResponse> getActionsByProgramme(String programmeId) {
        return actionRepository.findByProgrammeId(programmeId).stream()
                .map(referentielMapper::toActionResponse).toList();
    }

    @Override
    public List<ActionResponse> getActionsByProgrammeAndExercice(String programmeId, String exerciceId) {
        return actionRepository.findByProgrammeIdAndExerciceId(programmeId, exerciceId).stream()
                .map(referentielMapper::toActionResponse).toList();
    }

    @Override
    @Transactional
    public ActionResponse updateAction(String actionId, ActionRequest request) {
        final Action action = findAction(actionId);
        final Programme programme = findProgramme(request.programmeId());

        if (!action.getCodeAction().equals(request.codeAction()) &&
                actionRepository.existsByCodeActionAndProgrammeId(request.codeAction(), request.programmeId())) {
            throw new BusinessException(ErrorCode.ACTION_ALREADY_EXISTS, request.codeAction());
        }

        final Section section = findSection(request.sectionId());
        final Exercice exercice = findExercice(request.exerciceId());

        action.setCodeAction(request.codeAction());
        action.setLibelleFr(request.libelleFr());
        action.setLibelleEn(request.libelleEn());
        action.setNumero(request.numero());
        action.setCodeProgramme(programme.getCode());
        action.setCodeSection(section.getCodeSection());
        action.setCodeExercice(String.valueOf(exercice.getCodeExercice()));
        action.setNumeroProgramme(programme.getNumero());
        action.setProgramme(programme);
        action.setSection(section);
        action.setExercice(exercice);

        actionRepository.save(action);
        log.info("Updated action {}", actionId);
        return referentielMapper.toActionResponse(action);
    }

    @Override
    @Transactional
    public void deleteAction(String actionId) {
        final Action action = findAction(actionId);
        actionRepository.delete(action);
        log.info("Deleted action {}", actionId);
    }

    private Action findAction(String actionId) {
        return actionRepository.findById(actionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTION_NOT_FOUND, actionId));
    }

    private Programme findProgramme(String programmeId) {
        return programmeRepository.findById(programmeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, programmeId));
    }

    private Section findSection(String sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, sectionId));
    }

    private Exercice findExercice(String exerciceId) {
        return exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, exerciceId));
    }
}
