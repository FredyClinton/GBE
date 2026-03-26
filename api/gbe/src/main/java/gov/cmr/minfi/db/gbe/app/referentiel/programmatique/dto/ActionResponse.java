package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto;

import lombok.Builder;

@Builder
public record ActionResponse(
        String id,
        String codeAction,
        String autreCode,
        String libelleFr,
        String libelleEn,
        String programmeId,
        String programmeLibelle
) {
}