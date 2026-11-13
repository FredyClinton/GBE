package gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto;

import lombok.Builder;

@Builder
public record MatiereResponse(
        String id,
        String reference,
        String libelle
) {}
