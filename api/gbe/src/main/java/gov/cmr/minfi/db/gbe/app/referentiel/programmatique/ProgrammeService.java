package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ProgrammeResponse;

import java.util.List;

public interface ProgrammeService {

    List<ProgrammeResponse> getAllProgrammes();

    List<ProgrammeResponse> getProgrammesBySection(String sectionId);

    List<ProgrammeResponse> getProgrammesByExercice(String exerciceId);
}
