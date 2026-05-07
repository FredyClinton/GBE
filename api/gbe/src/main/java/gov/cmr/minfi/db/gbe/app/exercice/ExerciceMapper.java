package gov.cmr.minfi.db.gbe.app.exercice;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExerciceMapper {
    ExerciceResponse toResponse(Exercice exercice);
}
