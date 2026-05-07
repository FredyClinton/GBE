package gov.cmr.minfi.db.gbe.app.referentiel;

import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Chapitre;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.ChapitreResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.SectionResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Action;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ProgrammeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReferentielMapper {

    SectionResponse toSectionResponse(Section section);

    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionLibelle", source = "section.libelleFr")
    ProgrammeResponse toProgrammeResponse(Programme programme);

    @Mapping(target = "programmeId", source = "programme.id")
    @Mapping(target = "programmeLibelle", source = "programme.libelleFr")
    ActionResponse toActionResponse(Action action);

    @Mapping(target = "sectionId", source = "section.id")
    ChapitreResponse toChapitreResponse(Chapitre chapitre);
}
