package gov.cmr.minfi.db.gbe.app.depense.soumission;

import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.LigneSoumissionResponse;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.SoumissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SoumissionMapper {

    @Mapping(target = "fournisseurId", source = "fournisseur.id")
    @Mapping(target = "fournisseurRaisonSociale", source = "fournisseur.raisonSociale")
    @Mapping(target = "fournisseurNiu", source = "fournisseur.niu")
    @Mapping(target = "besoinId", source = "expressionBesoin.id")
    @Mapping(target = "besoinReference", source = "expressionBesoin.reference")
    @Mapping(target = "besoinObjet", source = "expressionBesoin.objet")
    @Mapping(target = "dateClotureSoumission", source = "expressionBesoin.dateClotureSoumission")
    @Mapping(target = "coordonneeBancaireId", source = "coordonneeBancaire.id")
    @Mapping(target = "rib", source = "coordonneeBancaire.rib")
    SoumissionResponse toResponse(Soumission soumission);

    @Mapping(target = "fournisseurId", source = "fournisseur.id")
    @Mapping(target = "fournisseurRaisonSociale", source = "fournisseur.raisonSociale")
    @Mapping(target = "fournisseurNiu", source = "fournisseur.niu")
    @Mapping(target = "besoinId", source = "expressionBesoin.id")
    @Mapping(target = "besoinReference", source = "expressionBesoin.reference")
    @Mapping(target = "besoinObjet", source = "expressionBesoin.objet")
    @Mapping(target = "dateClotureSoumission", source = "expressionBesoin.dateClotureSoumission")
    @Mapping(target = "coordonneeBancaireId", source = "coordonneeBancaire.id")
    @Mapping(target = "rib", source = "coordonneeBancaire.rib")
    @Mapping(target = "lignes", ignore = true)
    SoumissionResponse toResume(Soumission soumission);

    @Mapping(target = "ligneBesoinId", source = "ligneBesoin.id")
    @Mapping(target = "matiereReference", source = "ligneBesoin.matiere.reference")
    @Mapping(target = "matiereLibelle", source = "ligneBesoin.matiere.libelle")
    @Mapping(target = "quantiteDemandee", source = "ligneBesoin.quantiteDemandee")
    LigneSoumissionResponse toResponse(LigneSoumission ligne);
}
