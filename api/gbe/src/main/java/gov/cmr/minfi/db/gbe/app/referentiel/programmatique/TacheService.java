package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheResponse;

import java.util.List;

public interface TacheService {

	TacheResponse getTache(String tacheId);

	List<TacheResponse> getTachesByActivite(String activiteId);

	/** exerciceId facultatif. */
	List<TacheResponse> getTachesByProgramme(String programmeId, String exerciceId);

	TacheResponse createTache(TacheRequest request);

	TacheResponse updateTache(String tacheId, TacheRequest request);

	void deleteTache(String tacheId);
}
