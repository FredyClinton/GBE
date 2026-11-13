package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TacheResponse(
		String id,
		String code,
		String sigle,
		String libelle,
		String activiteId,
		String activiteCode,
		String actionId,
		String programmeId,
		String sectionId,
		String exerciceId
) {}
