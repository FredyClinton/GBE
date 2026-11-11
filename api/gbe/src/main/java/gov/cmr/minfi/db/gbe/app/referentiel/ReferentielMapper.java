package gov.cmr.minfi.db.gbe.app.referentiel;

import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Chapitre;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.ChapitreResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.dto.SectionResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomique;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.dto.NatureEconomiqueResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.Matiere;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Action;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Activite;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Tache;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActionResponse;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ProgrammeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReferentielMapper {

    SectionResponse toSectionResponse(Section section);

    NatureEconomiqueResponse toNatureEconomiqueResponse(NatureEconomique natureEconomique);

    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionLibelle", source = "section.libelleFr")
    ProgrammeResponse toProgrammeResponse(Programme programme);

    @Mapping(target = "programmeId", source = "programme.id")
    @Mapping(target = "programmeLibelle", source = "programme.libelleFr")
    ActionResponse toActionResponse(Action action);

    @Mapping(target = "sectionId", source = "section.id")
    ChapitreResponse toChapitreResponse(Chapitre chapitre);

    MatiereResponse toMatiereResponse(Matiere matiere);

    @Mapping(target = "actionId", source = "action.id")
    @Mapping(target = "actionCode", source = "action.codeAction")
    @Mapping(target = "programmeId", source = "programme.id")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "exerciceId", source = "exercice.id")
    ActiviteResponse toActiviteResponse(Activite activite);

    @Mapping(target = "activiteId", source = "activite.id")
    @Mapping(target = "activiteCode", source = "activite.code")
    @Mapping(target = "actionId", source = "action.id")
    @Mapping(target = "programmeId", source = "programme.id")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "exerciceId", source = "exercice.id")
    TacheResponse toTacheResponse(Tache tache);
}
