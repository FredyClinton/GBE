package gov.cmr.minfi.db.gbe.app.mandat.dto;

import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;

import java.time.LocalDate;

public record UpdateMandatRequest(
        RoleSysteme roleSysteme,
        LocalDate dateDebut,
        LocalDate dateFin,
        String numeroDecision

) {
}
