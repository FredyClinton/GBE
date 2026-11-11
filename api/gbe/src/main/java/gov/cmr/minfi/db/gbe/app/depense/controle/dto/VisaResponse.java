package gov.cmr.minfi.db.gbe.app.depense.controle.dto;

import gov.cmr.minfi.db.gbe.app.depense.controle.StatutVisa;
import gov.cmr.minfi.db.gbe.app.depense.controle.TypeVisa;
import gov.cmr.minfi.db.gbe.app.depense.engagement.StatutActe;

import java.time.LocalDateTime;

public record VisaResponse(
        String id,
        String numero,
        TypeVisa typeVisa,
        StatutVisa decision,
        LocalDateTime dateReception,
        LocalDateTime dateVisa,
        String observations,
        String referenceQuitus,
        String controleur,
        String acteId,
        String acteNumero,
        StatutActe acteStatut
) {
}
