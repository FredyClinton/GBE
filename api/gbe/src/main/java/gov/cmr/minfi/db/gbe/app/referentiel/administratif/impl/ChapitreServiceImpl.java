package gov.cmr.minfi.db.gbe.app.referentiel.administratif.impl;

import gov.cmr.minfi.db.gbe.app.referentiel.ReferentielMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.ChapitreRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.ChapitreService;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.ChapitreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapitreServiceImpl implements ChapitreService {

    private final ChapitreRepository chapitreRepository;
    private final ReferentielMapper referentielMapper;

    @Override
    public List<ChapitreResponse> getChapitresBySection(String sectionId) {
        return chapitreRepository.findBySectionId(sectionId).stream().map(referentielMapper::toChapitreResponse).toList();
    }

    @Override
    public List<ChapitreResponse> getChapitresByExercice(String exerciceId) {
        return chapitreRepository.findByExerciceId(exerciceId).stream().map(referentielMapper::toChapitreResponse).toList();
    }
}
