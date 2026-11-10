package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.ChapitreResponse;

import java.util.List;

public interface ChapitreService {

    List<ChapitreResponse> getChapitresBySection(String sectionId);

    List<ChapitreResponse> getChapitresByExercice(String exerciceId);
}
