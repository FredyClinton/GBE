package gov.cmr.minfi.db.gbe.app.referentiel.exercice;

import gov.cmr.minfi.db.gbe.app.referentiel.exercice.dto.ExerciceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExerciceMapper {
    ExerciceResponse toResponse(Exercice exercice);
}
