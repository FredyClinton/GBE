package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Action, programme, section et exercice sont déduits de l'activité. */
public record TacheRequest(
		@NotBlank @Size(max = 50) String code,
		@Size(max = 50) String sigle,
		@NotBlank String libelle,
		@NotBlank String activiteId
) {}
