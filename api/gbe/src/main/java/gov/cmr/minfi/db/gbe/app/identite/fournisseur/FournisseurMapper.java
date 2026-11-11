package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.CoordonneeBancaireResponse;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.FournisseurResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FournisseurMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "nomRepresentant", source = "user.lastName")
    @Mapping(target = "prenomRepresentant", source = "user.firstName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "telephone", source = "user.phoneNumber")
    @Mapping(target = "actif", source = "user.enabled")
    @Mapping(target = "ribs", source = "coordonneesBancaires")
    FournisseurResponse toResponse(Fournisseur fournisseur);

    CoordonneeBancaireResponse toResponse(CoordonneeBancaire coordonneeBancaire);
}
