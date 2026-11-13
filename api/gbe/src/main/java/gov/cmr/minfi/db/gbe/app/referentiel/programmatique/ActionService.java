package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionResponse;

import java.util.List;

public interface ActionService {
    ActionResponse createAction(ActionRequest request);

    ActionResponse getAction(String actionId);

    List<ActionResponse> getAllActions();

    List<ActionResponse> getActionsByProgramme(String programmeId);

    List<ActionResponse> getActionsByProgrammeAndExercice(String programmeId, String exerciceId);

    ActionResponse updateAction(String actionId, ActionRequest request);

    void deleteAction(String actionId);
}
