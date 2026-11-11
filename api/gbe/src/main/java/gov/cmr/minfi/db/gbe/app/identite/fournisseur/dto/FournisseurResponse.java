package gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FournisseurResponse(
        String id,
        String raisonSociale,
        String niu,
        String rccm,
        String adresse,
        String telephone,
        String userId,
        String nomRepresentant,
        String prenomRepresentant,
        String email,
        String telephoneRepresentant,
        boolean actif,
        List<CoordonneeBancaireResponse> ribs
) {}
