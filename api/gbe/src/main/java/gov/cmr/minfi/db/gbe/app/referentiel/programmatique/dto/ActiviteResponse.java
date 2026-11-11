package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActiviteResponse(
		String id,
		String code,
		String sigle,
		String libelle,
		String actionId,
		String actionCode,
		String programmeId,
		String sectionId,
		String exerciceId
) {}
