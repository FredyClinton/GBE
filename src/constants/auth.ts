// ============================================================
// FICHIER  : src/constants/auth.ts
// RÔLE     : Centralisation de toutes les constantes liées
//            à l'authentification : URLs API et routes internes.
//            Pour changer de back-end, modifier uniquement
//            la variable NEXT_PUBLIC_API_URL dans .env.local.
// ============================================================

/**
 * URL de base de l'API.
 * Définie via la variable d'environnement NEXT_PUBLIC_API_URL.
 * Valeur par défaut : serveur local sur le port 8080.
 */
export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080/api/v1';

/**
 * Endpoints de l'API d'authentification.
 * Préfixés par API_BASE_URL pour construire l'URL complète.
 */
export const AUTH_ENDPOINTS = {
  LOGIN:             `${API_BASE_URL}/auth/login`,
  REGISTER:          `${API_BASE_URL}/auth/register`,
  TWO_FACTOR_VERIFY: `${API_BASE_URL}/auth/2fa/verify`,
  TWO_FACTOR_RESEND: `${API_BASE_URL}/auth/2fa/resend`,
  FORGOT_PASSWORD:   `${API_BASE_URL}/auth/forgot-password`,
  RESET_PASSWORD:    `${API_BASE_URL}/auth/reset-password`,
  LOGOUT:            `${API_BASE_URL}/auth/logout`,
  REFRESH_TOKEN:     `${API_BASE_URL}/auth/refresh`,
} as const;

/**
 * Routes de navigation de l'application Next.js.
 * Utiliser ces constantes dans router.push() pour éviter
 * les chaînes de caractères codées en dur.
 */
export const APP_ROUTES = {
  LOGIN:           '/login',
  REGISTER:        '/register',
  TWO_FACTOR:      '/two-factor',
  FORGOT_PASSWORD: '/forgot-password',
  DASHBOARD:       '/dashboard',
} as const;

/**
 * Durée de validité du code 2FA en secondes (5 minutes).
 */
export const TWO_FACTOR_CODE_EXPIRY_SECONDS = 300;

/**
 * Nombre de chiffres du code OTP.
 */
export const TWO_FACTOR_CODE_LENGTH = 6;