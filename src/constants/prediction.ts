import {API_BASE_URL} from '@/constants/auth';

export const REFERENTIEL_ENDPOINTS = {
    SECTIONS: `${API_BASE_URL}/referentiel/sections`,
    NATURES_ECONOMIQUES: `${API_BASE_URL}/referentiel/natures-economiques`,
    EXERCICES: `${API_BASE_URL}/referentiel/exercices`,
} as const;


export const PREDICTION_ENDPOINTS = {
    PREDICT_AE: `${API_BASE_URL}/predictions/ae`,
} as const;