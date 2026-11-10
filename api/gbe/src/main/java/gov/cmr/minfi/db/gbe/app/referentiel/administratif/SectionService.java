package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.SectionResponse;

import java.util.List;

public interface SectionService {

    List<SectionResponse> getAllSections();

    List<SectionResponse> getSectionsByExercice(String exerciceId);
}
