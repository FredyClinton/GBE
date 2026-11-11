package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Programme, section et exercice sont déduits de l'action. */
public record ActiviteRequest(
		@NotBlank @Size(max = 50) String code,
		@Size(max = 50) String sigle,
		@NotBlank String libelle,
		@NotBlank String actionId
) {}
