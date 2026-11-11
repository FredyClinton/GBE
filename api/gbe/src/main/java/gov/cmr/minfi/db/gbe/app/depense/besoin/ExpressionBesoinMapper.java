package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinResponse;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.LigneBesoinResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpressionBesoinMapper {

    @Mapping(target = "tacheId", source = "tache.id")
    @Mapping(target = "tacheCode", source = "tache.code")
    @Mapping(target = "tacheLibelle", source = "tache.libelle")
    @Mapping(target = "programmeId", source = "programme.id")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "exerciceId", source = "exercice.id")
    @Mapping(target = "chapitreId", source = "chapitre.id")
    @Mapping(target = "chapitreCode", source = "chapitre.codeComplet")
    @Mapping(target = "natureEconomiqueId", source = "natureEconomique.id")
    @Mapping(target = "natureEconomiqueCode", source = "natureEconomique.code")
    @Mapping(target = "natureEconomiqueLibelle", source = "natureEconomique.libelleFr")
    ExpressionBesoinResponse toResponse(ExpressionBesoin besoin);

    /** Pour les listes : sans les lignes ni les libellés des référentiels (pas de chargement supplémentaire). */
    @Mapping(target = "tacheId", source = "tache.id")
    @Mapping(target = "tacheCode", source = "tache.code")
    @Mapping(target = "tacheLibelle", source = "tache.libelle")
    @Mapping(target = "programmeId", source = "programme.id")
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "exerciceId", source = "exercice.id")
    @Mapping(target = "chapitreId", source = "chapitre.id")
    @Mapping(target = "chapitreCode", ignore = true)
    @Mapping(target = "natureEconomiqueId", source = "natureEconomique.id")
    @Mapping(target = "natureEconomiqueCode", ignore = true)
    @Mapping(target = "natureEconomiqueLibelle", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    ExpressionBesoinResponse toResume(ExpressionBesoin besoin);

    @Mapping(target = "matiereId", source = "matiere.id")
    @Mapping(target = "matiereReference", source = "matiere.reference")
    @Mapping(target = "matiereLibelle", source = "matiere.libelle")
    LigneBesoinResponse toResponse(LigneBesoin ligne);
}
