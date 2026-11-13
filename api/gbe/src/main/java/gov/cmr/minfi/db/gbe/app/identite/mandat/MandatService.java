package gov.cmr.minfi.db.gbe.app.identite.mandat;

import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.identite.mandat.dto.CreateMandatRequest;
import gov.cmr.minfi.db.gbe.app.identite.mandat.dto.MandatSummary;
import gov.cmr.minfi.db.gbe.app.identite.mandat.dto.UpdateMandatRequest;

import java.util.List;

public interface MandatService {
    MandatSummary createMandat(String userId, CreateMandatRequest request);

    List<MandatSummary> getMandats(String userId);

    MandatSummary updateRole(String userId, String mandatId, RoleSysteme roleSysteme);

    MandatSummary updateMandat(String userId, String mandatId, UpdateMandatRequest request);

    void activateMandat(String userId, String mandatId);

    void deactivateMandat(String userId, String mandatId);

    void removeMandat(String userId, String mandatId);
}
