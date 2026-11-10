package gov.cmr.minfi.db.gbe.app.referentiel.administratif.impl;

import gov.cmr.minfi.db.gbe.app.referentiel.ReferentielMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionService;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.SectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final ReferentielMapper referentielMapper;

    @Override
    public List<SectionResponse> getAllSections() {
        return sectionRepository.findAll().stream().map(referentielMapper::toSectionResponse).toList();
    }

    @Override
    public List<SectionResponse> getSectionsByExercice(String exerciceId) {
        return sectionRepository.findByExerciceId(exerciceId).stream().map(referentielMapper::toSectionResponse).toList();
    }
}
