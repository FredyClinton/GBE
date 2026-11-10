package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.impl;

import gov.cmr.minfi.db.gbe.app.referentiel.ReferentielMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ProgrammeRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ProgrammeService;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ProgrammeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgrammeServiceImpl implements ProgrammeService {

    private final ProgrammeRepository programmeRepository;
    private final ReferentielMapper referentielMapper;

    @Override
    public List<ProgrammeResponse> getAllProgrammes() {
        return programmeRepository.findAll().stream().map(referentielMapper::toProgrammeResponse).toList();
    }

    @Override
    public List<ProgrammeResponse> getProgrammesBySection(String sectionId) {
        return programmeRepository.findBySectionId(sectionId).stream().map(referentielMapper::toProgrammeResponse).toList();
    }

    @Override
    public List<ProgrammeResponse> getProgrammesByExercice(String exerciceId) {
        return programmeRepository.findByExerciceId(exerciceId).stream().map(referentielMapper::toProgrammeResponse).toList();
    }
}
