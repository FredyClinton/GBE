package gov.cmr.minfi.db.gbe.app.depense.soumission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dépôt ou modification d'une soumission. Offre totale : un prix HT pour chaque ligne du besoin.
 * Le total HT est calculé par le serveur.
 */
public record SoumissionRequest(
        @NotBlank @Schema(description = "Ignoré en modification") String besoinId,
        @NotBlank @Schema(description = "Un des RIB du fournisseur, sur lequel il sera payé") String coordonneeBancaireId,
        @NotNull @Positive Integer delaiProposeJours,
        @Schema(description = "TTC de l'offre, au moins égal au total HT (méthode de calcul provisoire)")
        BigDecimal montantProposeTTC,
        @NotEmpty @Valid List<LigneSoumissionRequest> lignes
) {}
