package gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto;


import lombok.Builder;

@Builder
public record SectionResponse(
        String id,
        String codeSection,
        String sigle,
        String libelleFr,
        String libelleEn,
        String typeSection
) {
}
