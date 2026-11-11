package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteResponse;

import java.util.List;

public interface ActiviteService {

	ActiviteResponse getActivite(String activiteId);

	List<ActiviteResponse> getActivitesByAction(String actionId);

	/** exerciceId facultatif. */
	List<ActiviteResponse> getActivitesByProgramme(String programmeId, String exerciceId);

	ActiviteResponse createActivite(ActiviteRequest request);

	ActiviteResponse updateActivite(String activiteId, ActiviteRequest request);

	void deleteActivite(String activiteId);
}
