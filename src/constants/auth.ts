// ============================================================
// FICHIER  : src/constants/auth.ts
// RÔLE     : Centralise TOUTES les constantes de l'application.
//
// ✅ BACK-END CONNECTÉ : https://gbe-8clf.onrender.com
//
// Pour changer d'environnement, modifier uniquement
// NEXT_PUBLIC_API_URL dans .env.local — aucun autre fichier
// ne doit être touché.
// ============================================================

/**
 * URL de base de l'API back-end.
 *
 * En développement  : défini dans .env.local
 * En production     : défini dans les variables d'environnement du serveur
 *
 * Valeur par défaut → le back-end déployé sur Render
 */
export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL ?? 'https://gbe-8clf.onrender.com';

/**
 * Préfixe commun à tous les endpoints de l'API.
 * Si le back-end change de version (v2, v3…), modifier ici uniquement.
 */
const API_PREFIX = `${API_BASE_URL}/api/v1`;

/**
 * Endpoints d'authentification.
 *
 * POST /api/v1/auth/register  → Inscription
 * POST /api/v1/auth/login     → Connexion (retourne un sessionToken si 2FA actif)
 * POST /api/v1/auth/verify    → Vérification du code 2FA (TOTP)
 */
export const AUTH_ENDPOINTS = {
  REGISTER:    `${API_PREFIX}/auth/register`,
  LOGIN:       `${API_PREFIX}/auth/login`,
  VERIFY_2FA:  `${API_PREFIX}/auth/verify`,
} as const;

/**
 * Routes de navigation Next.js.
 * Utiliser ces constantes dans router.push() plutôt que
 * des chaînes codées en dur, pour faciliter les refactors.
 */
export const APP_ROUTES = {
  LOGIN:           '/login',
  REGISTER:        '/register',
  REGISTER_QR:     '/register/qrcode',   // Page QR code post-inscription
  TWO_FACTOR:      '/two-factor',         // Page saisie code OTP
  FORGOT_PASSWORD: '/forgot-password',
  DASHBOARD:       '/dashboard',          // Page d'accueil après connexion
} as const;

/**
 * Durée de validité du code 2FA en secondes (5 minutes).
 * Affichée dans le minuteur de la page two-factor.
 */
export const TWO_FACTOR_CODE_EXPIRY_SECONDS = 300;

/**
 * Nombre de chiffres du code OTP affiché par les cases.
 */
export const TWO_FACTOR_CODE_LENGTH = 6;