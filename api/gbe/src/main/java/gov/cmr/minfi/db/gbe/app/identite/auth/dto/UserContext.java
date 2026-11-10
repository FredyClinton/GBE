package gov.cmr.minfi.db.gbe.app.identite.auth.dto;

import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import lombok.Builder;

import java.util.List;

@Builder
public record UserContext(
        String userId,
        String firstName,
        String lastName,
        String email,
        String matricule,
        String nui,
        String cni,
        RoleSysteme role,
        List<MandatContext> mandats
) {

}
