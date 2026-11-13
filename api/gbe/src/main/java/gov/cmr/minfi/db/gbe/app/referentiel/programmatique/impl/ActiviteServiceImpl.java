package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.ReferentielMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Action;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Activite;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActiviteRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActiviteService;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.TacheRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActiviteServiceImpl implements ActiviteService {

	private final ActiviteRepository activiteRepository;
	private final TacheRepository tacheRepository;
	private final ActionRepository actionRepository;
	private final ReferentielMapper referentielMapper;

	@Override
	public ActiviteResponse getActivite(String activiteId) {
		return referentielMapper.toActiviteResponse(findActivite(activiteId));
	}

	@Override
	public List<ActiviteResponse> getActivitesByAction(String actionId) {
		return activiteRepository.findByActionIdOrderByCode(actionId).stream()
				.map(referentielMapper::toActiviteResponse).toList();
	}

	@Override
	public List<ActiviteResponse> getActivitesByProgramme(String programmeId, String exerciceId) {
		final List<Activite> activites = exerciceId == null
				? activiteRepository.findByProgrammeIdOrderByCode(programmeId)
				: activiteRepository.findByProgrammeIdAndExerciceIdOrderByCode(programmeId, exerciceId);
		return activites.stream().map(referentielMapper::toActiviteResponse).toList();
	}

	@Override
	@Transactional
	public ActiviteResponse createActivite(ActiviteRequest request) {
		final Action action = findAction(request.actionId());
		final String code = request.code().trim();
		if (activiteRepository.existsByActionIdAndCode(action.getId(), code)) {
			throw new BusinessException(ErrorCode.ACTIVITE_ALREADY_EXISTS, code);
		}
		final Activite activite = Activite.builder()
				.code(code)
				.sigle(request.sigle())
				.libelle(request.libelle())
				.build();
		activite.rattacherA(action);
		activiteRepository.save(activite);
		log.info("Activité {} créée dans l'action {}", code, action.getCodeAction());
		return referentielMapper.toActiviteResponse(activite);
	}

	@Override
	@Transactional
	public ActiviteResponse updateActivite(String activiteId, ActiviteRequest request) {
		final Activite activite = findActivite(activiteId);
		final Action action = findAction(request.actionId());
		final String code = request.code().trim();
		if (activiteRepository.existsByActionIdAndCodeAndIdNot(action.getId(), code, activiteId)) {
			throw new BusinessException(ErrorCode.ACTIVITE_ALREADY_EXISTS, code);
		}
		final boolean changementAction = !activite.getAction().getId().equals(action.getId());
		activite.setCode(code);
		activite.setSigle(request.sigle());
		activite.setLibelle(request.libelle());
		activite.rattacherA(action);
		activiteRepository.save(activite);
		if (changementAction) {
			// Les tâches recopient la hiérarchie de leur activité : on la propage.
			tacheRepository.findByActiviteId(activiteId).forEach(t -> t.rattacherA(activite));
		}
		log.info("Activité {} modifiée", activiteId);
		return referentielMapper.toActiviteResponse(activite);
	}

	@Override
	@Transactional
	public void deleteActivite(String activiteId) {
		final Activite activite = findActivite(activiteId);
		if (tacheRepository.existsByActiviteId(activiteId)) {
			throw new BusinessException(ErrorCode.ACTIVITE_A_DES_TACHES, activite.getCode());
		}
		activiteRepository.delete(activite);
		log.info("Activité {} supprimée", activiteId);
	}

	private Activite findActivite(String activiteId) {
		return activiteRepository.findById(activiteId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITE_NOT_FOUND, activiteId));
	}

	private Action findAction(String actionId) {
		return actionRepository.findById(actionId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACTION_NOT_FOUND, actionId));
	}
}
