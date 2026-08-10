// ============================================================
// FICHIER  : src/lib/referentielService.ts
// RÔLE     : Chargement des listes utilisées dans les dropdowns
//            du formulaire de prédiction (sections, natures
//            économiques, exercices). Appels authentifiés.
// ============================================================

import {REFERENTIEL_ENDPOINTS} from '@/constants/prediction';
import {getAccessToken} from '@/lib/session';
import {ExerciceOption, NatureEconomiqueOption, SectionOption,} from '@/types/prediction';

async function getJson<T>(url: string): Promise<T> {
    const token = getAccessToken();

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            ...(token ? {Authorization: `Bearer ${token}`} : {}),
        },
    });

    if (!response.ok) {
        throw new Error(`Erreur HTTP ${response.status} sur ${url}`);
    }

    return await response.json() as Promise<T>;
}

export function getSections(): Promise<SectionOption[]> {
    return getJson<SectionOption[]>(REFERENTIEL_ENDPOINTS.SECTIONS);
}

export function getNaturesEconomiques(): Promise<NatureEconomiqueOption[]> {
    return getJson<NatureEconomiqueOption[]>(REFERENTIEL_ENDPOINTS.NATURES_ECONOMIQUES);
}

export function getExercices(): Promise<ExerciceOption[]> {
    return getJson<ExerciceOption[]>(REFERENTIEL_ENDPOINTS.EXERCICES);
}