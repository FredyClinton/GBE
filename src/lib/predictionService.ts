// ============================================================
// FICHIER  : src/lib/predictionService.ts
// RÔLE     : Appel du endpoint de prédiction AE (POST, protégé
//            par JWT). Traduit le 503 GBE+ en message clair.
// ============================================================

import {PREDICTION_ENDPOINTS} from '@/constants/prediction';
import {getAccessToken} from '@/lib/session';
import {PredictionAEPayload, PredictionAEResult} from '@/types/prediction';

export async function predireProfilAE(
    payload: PredictionAEPayload
): Promise<PredictionAEResult> {
    const token = getAccessToken();

    const response = await fetch(PREDICTION_ENDPOINTS.PREDICT_AE, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(token ? {Authorization: `Bearer ${token}`} : {}),
        },
        body: JSON.stringify(payload),
    });

    if (response.status === 503) {
        // GBE+ répond 503 quand le modèle n'a pas pu être chargé au démarrage.
        throw new Error(
            'Le service de prédiction est momentanément indisponible (modèle non chargé). Réessayez dans quelques instants.'
        );
    }

    if (!response.ok) {
        const body = await response.json().catch(() => null);
        throw new Error(body?.message || `Erreur HTTP ${response.status} lors de la prédiction.`);
    }

    return await response.json() as Promise<PredictionAEResult>;
}