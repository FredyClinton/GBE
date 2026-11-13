package gov.cmr.minfi.db.gbe.app.referentiel.exercice.dto;

import lombok.Builder;

@Builder
public record ExerciceResponse(
        String id,
        Integer annee,
        Integer codeExercice,
        String libelleFr,
        String libelleEn,
        boolean actif
) {
}
