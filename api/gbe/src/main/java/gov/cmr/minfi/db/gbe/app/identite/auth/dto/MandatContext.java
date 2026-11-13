package gov.cmr.minfi.db.gbe.app.identite.auth.dto;

import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record MandatContext(
        String mandatId,
        RoleSysteme roleSysteme,
        String sectionId,
        String sectionLibelle,
        String sectionCode,
        String programmeId,
        String programmeLibelle,
        String programmeCode,
        Set<Permission> permissions,
        LocalDate dateDebut,
        LocalDate dateFin,
        String numeroDecision,
        boolean actif,
        boolean valide
) {
}
