package gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChapitreResponse(
	String id,
	String codeComplet,
	String codeChap,
	String libelleFr,
	String libelleEn,
	String sectionId
) {}
