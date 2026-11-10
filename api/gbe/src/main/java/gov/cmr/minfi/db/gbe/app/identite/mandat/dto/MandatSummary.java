package gov.cmr.minfi.db.gbe.app.identite.mandat.dto;

import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MandatSummary(
        String mandatId,
        RoleSysteme roleSysteme,
        String sectionId,
        String sectionLibelle,
        String programmeId,
        String programmeLibelle,
        LocalDate dateDebut,
        LocalDate dateFin,
        String numeroDecision,
        boolean actif,
        boolean valide
) {
}
