export interface ExerciceOption {
    id: string;
    annee: number;
    codeExercice: number;      // = valeur "exercice" attendue par GBE+ (47 + année-2013)
    libelleFr: string;
}

export interface SectionOption {
    id: string;
    codeSection: string;      // 2 caractères, ex: "20" — ministère
    libelleFr: string;
}

export interface NatureEconomiqueOption {
    id: string;
    code: string;              // code de rubrique économique
    libelleFr: string;
}

export interface PredictionAEPayload {
    exercice: number;
    chapitreCode: string;      // = SectionOption.codeSection (PAS un code Chapitre)
    rubriqueCode: string;      // = NatureEconomiqueOption.code
    dotationAE: number;
}

export interface PredictionAEResult {
    exercice: number;
    chapitreCode: string;
    rubriqueCode: string;
    /** 12 poids mensuels (janvier → décembre), somme ≈ 1. */
    profilPredit: number[];
    /** FRONTLOAD | BACKLOAD | LINEAIRE | IRREGULIER, absent si non calculable. */
    archetype?: string;
    /** exact | chapitre | cold_start — fiabilité de la prédiction. */
    niveauHistorique?: string;
    /** Présent si chapitreCode/rubriqueCode inconnus à l'entraînement. */
    avertissement?: string;
    /** profilPredit × dotationAE, en FCFA, mois par mois. */
    montantsPredits?: number[];
}

export interface PredictionFormErrors {
    exercice?: string;
    chapitreCode?: string;
    rubriqueCode?: string;
    dotationAE?: string;
}