package gov.cmr.minfi.db.gbe.app.admin;

import gov.cmr.minfi.db.gbe.app.admin.dto.RoleResponse;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(target = "code", expression = "java(role.name())")
    RoleResponse toRoleResponse(RoleSysteme role);
}
