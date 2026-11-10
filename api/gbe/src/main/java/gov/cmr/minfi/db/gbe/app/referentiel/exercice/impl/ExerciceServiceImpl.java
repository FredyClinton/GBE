package gov.cmr.minfi.db.gbe.app.referentiel.exercice.impl;

import gov.cmr.minfi.db.gbe.app.referentiel.exercice.ExerciceMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.ExerciceRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.ExerciceService;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.dto.ExerciceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciceServiceImpl implements ExerciceService {

    private final ExerciceRepository exerciceRepository;
    private final ExerciceMapper exerciceMapper;

    @Override
    public List<ExerciceResponse> getAllExercices() {
        return exerciceRepository.findAll().stream().map(exerciceMapper::toResponse).toList();
    }
}
