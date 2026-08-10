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
  LOGIN:          `${API_BASE_URL}/auth/login`,
  VERIFY:         `${API_BASE_URL}/auth/verify`,     // Code TOTP à chaque connexion (MFA déjà activé)
  SETUP_MFA:      `${API_BASE_URL}/auth/setup-mfa`,  // Enrôlement MFA (1ère connexion, scan du QR)
  REFRESH_TOKEN:  `${API_BASE_URL}/auth/refresh`,
  // ⚠️ Pas encore exposés par l'API (absents du openapi.json) :
  FORGOT_PASSWORD: `${API_BASE_URL}/auth/forgot-password`,
  RESET_PASSWORD:  `${API_BASE_URL}/auth/reset-password`,
} as const;

/**
 * Routes de navigation de l'application Next.js.
 * Utiliser ces constantes dans router.push() pour éviter
 * les chaînes de caractères codées en dur.
 */
export const APP_ROUTES = {
  LOGIN:           '/login',
  TWO_FACTOR:      '/two-factor',
  MFA_SETUP:       '/mfa-setup',
  FORGOT_PASSWORD: '/forgot-password',
  DASHBOARD:       '/dashboard',
} as const;

/**
 * Durée de validité du mfaToken en secondes (5 minutes,
 * déduite du JWT renvoyé par le back-end : exp - iat = 300).
 */
export const TWO_FACTOR_CODE_EXPIRY_SECONDS = 300;

/**
 * Nombre de chiffres du code OTP.
 */
export const TWO_FACTOR_CODE_LENGTH = 6;