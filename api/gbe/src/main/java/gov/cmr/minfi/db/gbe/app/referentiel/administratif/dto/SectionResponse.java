package gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SectionResponse(String id, String codeSection, String sigle, String libelleFr, String libelleEn) {}
