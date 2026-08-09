package gov.cmr.minfi.db.gbe.app.prediction.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Miroir exact de ProfilAE (gbe_plus_api/schemas.py). */
record GbePlusPredictionResponse(
        int exercice,
        String chapitre,
        String rubrique,
        @JsonProperty("profil_predit") List<Double> profilPredit,
        String archetype,
        @JsonProperty("niveau_historique") String niveauHistorique,
        String avertissement,
        @JsonProperty("montants_predits") List<Double> montantsPredits
) {}
