package gov.cmr.minfi.db.gbe.app.admin.dto;

import gov.cmr.minfi.db.gbe.app.iam.permission.Permission;
import lombok.Builder;

import java.util.Set;

@Builder
public record RoleResponse(
        String code,
        String libelle,
        Set<Permission> defaultPermissions
) {
}
