// ============================================================
// FICHIER  : src/lib/predictionService.mock.ts
// RÔLE     : Simulateur local de predireProfilAE(), pour itérer sur
//            l'affichage du résultat (barres, badges, fiabilité)
//            sans dépendre du backend Spring / GBE+.
//            Ne PAS utiliser en production — voir USE_MOCK_PREDICTION
//            dans dashboard/predictions/page.tsx.
// ============================================================

import {PredictionAEPayload, PredictionAEResult} from '@/types/prediction';

// Poids mensuels (janvier → décembre), chacun sommant à ≈ 1.
const PROFIL_FRONTLOAD = [0.16, 0.15, 0.12, 0.10, 0.09, 0.08, 0.07, 0.06, 0.06, 0.05, 0.03, 0.03];
const PROFIL_BACKLOAD = [0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.10, 0.12, 0.16, 0.18];
const PROFIL_IRREGULIER = [0.11, 0.04, 0.14, 0.03, 0.09, 0.02, 0.13, 0.05, 0.10, 0.06, 0.15, 0.08];

function sommeCodesCaracteres(s: string): number {
    let total = 0;
    for (let i = 0; i < s.length; i++) {
        total += s.charCodeAt(i);
    }
    return total;
}

function arrondirProfil(profil: number[], dotationAE: number): number[] {
    return profil.map((poids) => Math.round(poids * dotationAE));
}

/**
 * Version simulée de predireProfilAE — même signature, mais résout
 * localement après un délai réseau simulé (600-900ms) et choisit un
 * profil de manière déterministe à partir du chapitreCode et de la
 * dotationAE, pour que les mêmes entrées donnent toujours le même résultat.
 */
export function predireProfilAEMock(
    payload: PredictionAEPayload
): Promise<PredictionAEResult> {
    const delaiMs = 600 + Math.round(Math.random() * 300);

    return new Promise((resolve) => {
        setTimeout(() => {
            const cle = (sommeCodesCaracteres(payload.chapitreCode + payload.rubriqueCode) + payload.dotationAE) % 3;

            let result: PredictionAEResult;

            if (cle === 0) {
                result = {
                    exercice: payload.exercice,
                    chapitreCode: payload.chapitreCode,
                    rubriqueCode: payload.rubriqueCode,
                    profilPredit: PROFIL_FRONTLOAD,
                    archetype: 'FRONTLOAD',
                    niveauHistorique: 'exact',
                    montantsPredits: arrondirProfil(PROFIL_FRONTLOAD, payload.dotationAE),
                };
            } else if (cle === 1) {
                result = {
                    exercice: payload.exercice,
                    chapitreCode: payload.chapitreCode,
                    rubriqueCode: payload.rubriqueCode,
                    profilPredit: PROFIL_BACKLOAD,
                    archetype: 'BACKLOAD',
                    niveauHistorique: 'chapitre',
                    montantsPredits: arrondirProfil(PROFIL_BACKLOAD, payload.dotationAE),
                };
            } else {
                result = {
                    exercice: payload.exercice,
                    chapitreCode: payload.chapitreCode,
                    rubriqueCode: payload.rubriqueCode,
                    profilPredit: PROFIL_IRREGULIER,
                    archetype: 'IRREGULIER',
                    niveauHistorique: 'cold_start',
                    avertissement:
                        "Chapitre ou rubrique absent des données d'entraînement — prédiction à faible fiabilité.",
                    montantsPredits: arrondirProfil(PROFIL_IRREGULIER, payload.dotationAE),
                };
            }

            resolve(result);
        }, delaiMs);
    });
}