package gov.cmr.minfi.db.gbe.app.mandat;

import gov.cmr.minfi.db.gbe.app.auth.dto.response.MandatContext;
import gov.cmr.minfi.db.gbe.app.mandat.dto.MandatSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MandatMapper {

    @Mapping(target = "mandatId", source = "id")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionLibelle", source = "section.libelleFr")
    @Mapping(target = "programmeId", source = "programme.id")
    @Mapping(target = "programmeLibelle", source = "programme.libelleFr")
    MandatSummary toSummary(Mandat mandat);

    List<MandatSummary> toSummaryList(List<Mandat> mandats);

    @Mapping(target = "mandatId", source = "id")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionLibelle", source = "section.libelleFr")
    @Mapping(target = "sectionCode", source = "section.codeSection")
    @Mapping(target = "programmeId", source = "programme.id")
    @Mapping(target = "programmeLibelle", source = "programme.libelleFr")
    @Mapping(target = "programmeCode", source = "programme.code")
    MandatContext toContext(Mandat mandat);

    List<MandatContext> toContextList(List<Mandat> mandats);
}
