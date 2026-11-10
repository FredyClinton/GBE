package gov.cmr.minfi.db.gbe.app.identite.mandat.dto;

import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "VALIDATION.AFFECTATION.ROLE.NOT_NULL")
        RoleSysteme roleSysteme
) {
}
