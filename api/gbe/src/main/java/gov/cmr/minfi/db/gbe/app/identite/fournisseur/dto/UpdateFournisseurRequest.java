package gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Informations de l'entreprise ; le compte de connexion se gère par les routes utilisateur. */
public record UpdateFournisseurRequest(
        @NotBlank String raisonSociale,
        @NotBlank @Size(max = 50) String niu,
        @Size(max = 100) String rccm,
        String adresse
) {}
