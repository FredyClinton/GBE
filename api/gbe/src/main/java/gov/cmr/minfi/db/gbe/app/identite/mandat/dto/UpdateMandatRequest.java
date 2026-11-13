package gov.cmr.minfi.db.gbe.app.identite.mandat.dto;

import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;

import java.time.LocalDate;

public record UpdateMandatRequest(
        RoleSysteme roleSysteme,
        LocalDate dateDebut,
        LocalDate dateFin,
        String numeroDecision

) {
}
