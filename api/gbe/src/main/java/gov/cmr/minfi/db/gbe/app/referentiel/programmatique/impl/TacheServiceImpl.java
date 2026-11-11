package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.ReferentielMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Activite;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActiviteRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Tache;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.TacheRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.TacheService;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TacheServiceImpl implements TacheService {

	private final TacheRepository tacheRepository;
	private final ActiviteRepository activiteRepository;
	private final ReferentielMapper referentielMapper;

	@Override
	public TacheResponse getTache(String tacheId) {
		return referentielMapper.toTacheResponse(findTache(tacheId));
	}

	@Override
	public List<TacheResponse> getTachesByActivite(String activiteId) {
		return tacheRepository.findByActiviteIdOrderByCode(activiteId).stream()
				.map(referentielMapper::toTacheResponse).toList();
	}

	@Override
	public List<TacheResponse> getTachesByProgramme(String programmeId, String exerciceId) {
		final List<Tache> taches = exerciceId == null
				? tacheRepository.findByProgrammeIdOrderByCode(programmeId)
				: tacheRepository.findByProgrammeIdAndExerciceIdOrderByCode(programmeId, exerciceId);
		return taches.stream().map(referentielMapper::toTacheResponse).toList();
	}

	@Override
	@Transactional
	public TacheResponse createTache(TacheRequest request) {
		final Activite activite = findActivite(request.activiteId());
		final String code = request.code().trim();
		if (tacheRepository.existsByActiviteIdAndCode(activite.getId(), code)) {
			throw new BusinessException(ErrorCode.TACHE_ALREADY_EXISTS, code);
		}
		final Tache tache = Tache.builder()
				.code(code)
				.sigle(request.sigle())
				.libelle(request.libelle())
				.build();
		tache.rattacherA(activite);
		tacheRepository.save(tache);
		log.info("Tâche {} créée dans l'activité {}", code, activite.getCode());
		return referentielMapper.toTacheResponse(tache);
	}

	@Override
	@Transactional
	public TacheResponse updateTache(String tacheId, TacheRequest request) {
		final Tache tache = findTache(tacheId);
		final Activite activite = findActivite(request.activiteId());
		final String code = request.code().trim();
		if (tacheRepository.existsByActiviteIdAndCodeAndIdNot(activite.getId(), code, tacheId)) {
			throw new BusinessException(ErrorCode.TACHE_ALREADY_EXISTS, code);
		}
		tache.setCode(code);
		tache.setSigle(request.sigle());
		tache.setLibelle(request.libelle());
		tache.rattacherA(activite);
		tacheRepository.save(tache);
		log.info("Tâche {} modifiée", tacheId);
		return referentielMapper.toTacheResponse(tache);
	}

	@Override
	@Transactional
	public void deleteTache(String tacheId) {
		tacheRepository.delete(findTache(tacheId));
		log.info("Tâche {} supprimée", tacheId);
	}

	private Tache findTache(String tacheId) {
		return tacheRepository.findById(tacheId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TACHE_NOT_FOUND, tacheId));
	}

	private Activite findActivite(String activiteId) {
		return activiteRepository.findById(activiteId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITE_NOT_FOUND, activiteId));
	}
}
