package gov.cmr.minfi.db.gbe.app.prediction.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PredictionAEResponse(
        Integer exercice,
        String chapitreCode,
        String rubriqueCode,

        /** 12 poids mensuels (janv -> déc), somme ≈ 1. */
        List<Double> profilPredit,

        /** FRONTLOAD / BACKLOAD / LINEAIRE / IRREGULIER, null si non calculable. */
        String archetype,

        /** exact / chapitre / cold_start — transparence sur la fiabilité. */
        String niveauHistorique,

        /** Renseigné si chapitreCode ou rubriqueCode inconnus à l'entraînement. */
        String avertissement,

        /** profilPredit × dotationAE, en FCFA, mois par mois. */
        List<Double> montantsPredits
) {}
