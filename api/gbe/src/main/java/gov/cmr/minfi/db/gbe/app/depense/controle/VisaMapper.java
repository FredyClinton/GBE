package gov.cmr.minfi.db.gbe.app.depense.controle;

import gov.cmr.minfi.db.gbe.app.depense.controle.dto.VisaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VisaMapper {

    @Mapping(target = "controleur", source = "createdBy")
    @Mapping(target = "acteId", source = "acteJuridique.id")
    @Mapping(target = "acteNumero", source = "acteJuridique.numero")
    @Mapping(target = "acteStatut", source = "acteJuridique.statut")
    VisaResponse toResponse(Visa visa);
}
