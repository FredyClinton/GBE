package gov.cmr.minfi.db.gbe.app.referentiel.matiere.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.ReferentielMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.Matiere;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.MatiereRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.MatiereService;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatiereServiceImpl implements MatiereService {

    private final MatiereRepository matiereRepository;
    private final ReferentielMapper referentielMapper;

    @Override
    public List<MatiereResponse> getAllMatieres() {
        return matiereRepository.findAllByOrderByReferenceAsc().stream()
                .map(referentielMapper::toMatiereResponse)
                .toList();
    }

    @Override
    public MatiereResponse getMatiere(String matiereId) {
        return referentielMapper.toMatiereResponse(findMatiere(matiereId));
    }

    @Override
    @Transactional
    public MatiereResponse createMatiere(MatiereRequest request) {
        final String reference = request.reference().trim();
        if (matiereRepository.existsByReference(reference)) {
            throw new BusinessException(ErrorCode.MATIERE_ALREADY_EXISTS, reference);
        }
        final Matiere matiere = Matiere.builder()
                .reference(reference)
                .libelle(request.libelle())
                .build();
        matiereRepository.save(matiere);
        log.info("Matière créée : {}", reference);
        return referentielMapper.toMatiereResponse(matiere);
    }

    @Override
    @Transactional
    public MatiereResponse updateMatiere(String matiereId, MatiereRequest request) {
        final Matiere matiere = findMatiere(matiereId);
        final String reference = request.reference().trim();
        if (matiereRepository.existsByReferenceAndIdNot(reference, matiereId)) {
            throw new BusinessException(ErrorCode.MATIERE_ALREADY_EXISTS, reference);
        }
        matiere.setReference(reference);
        matiere.setLibelle(request.libelle());
        matiereRepository.save(matiere);
        log.info("Matière modifiée : {}", matiereId);
        return referentielMapper.toMatiereResponse(matiere);
    }

    @Override
    @Transactional
    public void deleteMatiere(String matiereId) {
        matiereRepository.delete(findMatiere(matiereId));
        log.info("Matière supprimée : {}", matiereId);
    }

    private Matiere findMatiere(String matiereId) {
        return matiereRepository.findById(matiereId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATIERE_NOT_FOUND, matiereId));
    }
}
