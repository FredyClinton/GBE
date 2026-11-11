package gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Création d'un fournisseur et de son compte de connexion par l'administrateur. */
public record CreateFournisseurRequest(

        // ── Entreprise ────────────────────────────────────
        @NotBlank String raisonSociale,
        @NotBlank @Size(max = 50) String niu,
        @Size(max = 100) String rccm,
        String adresse,

        // ── Compte de connexion (représentant) ────────────
        @NotBlank String nomRepresentant,
        @NotBlank String prenomRepresentant,
        @NotBlank @Email String email,
        @NotBlank String telephone,

        @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$", message = "VALIDATION.USER.PASSWORD.WEAK")
        @Schema(description = "Mot de passe provisoire : changement imposé à la première connexion", example = "Temp@1234")
        String password,

        @Schema(description = "RIB à 23 caractères (espaces et tirets acceptés)")
        List<String> ribs
) {}
