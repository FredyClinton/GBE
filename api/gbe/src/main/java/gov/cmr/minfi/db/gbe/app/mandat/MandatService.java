package gov.cmr.minfi.db.gbe.app.mandat;

import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.mandat.dto.CreateMandatRequest;
import gov.cmr.minfi.db.gbe.app.mandat.dto.MandatSummary;
import gov.cmr.minfi.db.gbe.app.mandat.dto.UpdateMandatRequest;

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
