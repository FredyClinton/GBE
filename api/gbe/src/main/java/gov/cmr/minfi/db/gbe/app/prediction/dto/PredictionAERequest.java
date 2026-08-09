package gov.cmr.minfi.db.gbe.app.prediction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Requête de prédiction du profil d'engagement AE (12 valeurs mensuelles).
 *
 * ATTENTION au mapping (validé le 03/08) :
 *   - chapitreCode  = Section.codeSection  (2 caractères, ex: "20" = MINFI)
 *                     PAS Chapitre.codeComplet — le "chapitre" du modèle ML
 *                     (chCode dans engagementAE.csv) correspond à la Section
 *                     GBE, pas au Chapitre GBE (qui, lui, correspond à la
 *                     structureAdministrative DGB).
 *   - rubriqueCode  = NatureEconomique.code
 */
public record PredictionAERequest(
        @NotNull Integer exercice,
        @NotBlank String chapitreCode,
        @NotBlank String rubriqueCode,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal dotationAE
) {}
