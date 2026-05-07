package gov.cmr.minfi.db.gbe.app.budget.credit;

import gov.cmr.minfi.db.gbe.app.budget.credit.dto.CreditBudgetaireResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditBudgetaireMapper {

    @Mapping(target = "exerciceId", source = "exercice.id")
    @Mapping(target = "exerciceAnnee", source = "exercice.annee")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "sectionLibelle", source = "section.libelleFr")
    @Mapping(target = "sectionCode", source = "section.codeSection")
    @Mapping(target = "programmeId", source = "programme.id")
    @Mapping(target = "programmeLibelle", source = "programme.libelleFr")
    @Mapping(target = "programmeCode", source = "programme.code")
    @Mapping(target = "actionId", source = "action.id")
    @Mapping(target = "actionLibelle", source = "action.libelleFr")
    @Mapping(target = "chapitreId", source = "chapitre.id")
    @Mapping(target = "chapitreLibelle", source = "chapitre.libelleFr")
    @Mapping(target = "chapitreTutelleId", ignore = true)
    @Mapping(target = "chapitreTutelleLibelle", ignore = true)
    @Mapping(target = "creditParentId", source = "creditParent.id")
    @Mapping(target = "creditParentCodeImputation", source = "creditParent.codeImputation")
    CreditBudgetaireResponse toResponse(CreditBudgetaire credit);

    List<CreditBudgetaireResponse> toResponseList(List<CreditBudgetaire> credits);
}
