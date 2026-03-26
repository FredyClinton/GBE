package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto;

import lombok.Builder;

@Builder
public record ProgrammeResponse(
        String id,
        String code,
        String autreCode,
        String libelleFr,
        String libelleEn,
        String sectionId,
        String sectionLibelle,
        boolean actif
) {
}