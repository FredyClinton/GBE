package gov.cmr.minfi.db.gbe.app.auth;

import gov.cmr.minfi.db.gbe.app.auth.dto.response.MandatContext;
import gov.cmr.minfi.db.gbe.app.auth.dto.response.UserContext;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", imports = {RoleSysteme.class})
public interface AuthMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "matricule", source = "user.matricule")
    @Mapping(target = "nui", source = "user.nui")
    @Mapping(target = "cni", source = "user.numeroCni")
    @Mapping(target = "role", expression = "java(user.getRole() != null ? RoleSysteme.valueOf(user.getRole().getName().replace(\"ROLE_\", \"\")) : null)")
    @Mapping(target = "mandats", source = "mandatContexts")
    UserContext toUserContext(User user, List<MandatContext> mandatContexts);
}
