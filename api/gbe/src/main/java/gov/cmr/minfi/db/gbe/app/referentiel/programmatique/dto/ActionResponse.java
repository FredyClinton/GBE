package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActionResponse(
	String id,
	String codeAction,
	String libelleFr,
	String libelleEn,
	String programmeId,
	String programmeLibelle
) {}
