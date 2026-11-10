package gov.cmr.minfi.db.gbe.app.referentiel.exercice;

import gov.cmr.minfi.db.gbe.app.referentiel.exercice.dto.ExerciceResponse;

import java.util.List;

public interface ExerciceService {

    List<ExerciceResponse> getAllExercices();
}
