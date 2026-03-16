// ============================================================
// FICHIER  : src/lib/authService.ts
// RÔLE     : Couche d'abstraction entre les composants React
//            et l'API back-end GBE (https://gbe-8clf.onrender.com).
//
// Principe : les composants n'appellent JAMAIS fetch() directement.
//            Ils appellent les fonctions de ce fichier, qui :
//              1. Construisent la requête HTTP
//              2. Gèrent les erreurs réseau
//              3. Retournent des objets typés
//
// Pour changer de back-end : modifier uniquement AUTH_ENDPOINTS
// dans src/constants/auth.ts — ce fichier ne change pas.
// ============================================================

import {
  LoginPayload,
  RegisterPayload,
  VerifyPayload,
  ForgotPasswordPayload,
  LoginResponse,
  RegisterResponse,
  VerifyResponse,
  GenericResponse,
} from '@/types/auth';
import { AUTH_ENDPOINTS } from '@/constants/auth';

// ── En-têtes par défaut pour toutes les requêtes JSON ──────
const JSON_HEADERS: HeadersInit = {
  'Content-Type': 'application/json',
  'Accept':       'application/json',
};

// ─────────────────────────────────────────────────────────────
// UTILITAIRE INTERNE
// ─────────────────────────────────────────────────────────────

/**
 * Exécute un POST JSON vers l'API et retourne le corps parsé.
 *
 * Gestion des erreurs :
 * - Erreur réseau (serveur injoignable) → throw Error
 * - Réponse HTTP ≥ 400 → throw Error avec le message du serveur
 * - Réponse 2xx → retourne le JSON parsé
 *
 * @param url     URL complète de l'endpoint
 * @param payload Objet à sérialiser en JSON dans le corps
 * @param token   (optionnel) JWT Bearer pour les routes protégées
 */
async function postJson<TPayload, TResponse>(
  url: string,
  payload: TPayload,
  token?: string
): Promise<TResponse> {
  // Construction des en-têtes : ajouter Authorization si token fourni
  const headers: HeadersInit = { ...JSON_HEADERS };
  if (token) {
    (headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
  }

  let response: Response;
  try {
    response = await fetch(url, {
      method:  'POST',
      headers,
      body:    JSON.stringify(payload),
    });
  } catch {
    // Erreur réseau (CORS, serveur éteint, pas d'internet…)
    throw new Error(
      'Impossible de joindre le serveur. Vérifiez votre connexion internet.'
    );
  }

  // Lire le corps de la réponse (même en cas d'erreur HTTP)
  const data = await response.json().catch(() => ({
    success: false,
    message: `Erreur HTTP ${response.status} — réponse invalide du serveur.`,
  })) as TResponse & { message?: string; success?: boolean };

  // Si le serveur retourne un code d'erreur (4xx, 5xx)
  if (!response.ok) {
    const errMsg = data.message || `Erreur HTTP ${response.status}`;
    throw new Error(errMsg);
  }

  return data;
}

// ─────────────────────────────────────────────────────────────
// FONCTIONS PUBLIQUES — une par endpoint
// ─────────────────────────────────────────────────────────────

/**
 * INSCRIPTION
 * Route : POST /api/v1/auth/register
 *
 * Envoie les informations du formulaire d'inscription.
 * Si mfaEnabled=true, le back-end retourne une qrCodeUrl
 * que l'utilisateur doit scanner avec son téléphone.
 *
 * @param payload Données du formulaire d'inscription
 * @returns Réponse contenant éventuellement qrCodeUrl
 */
export async function registerUser(
  payload: RegisterPayload
): Promise<RegisterResponse> {
  return postJson<RegisterPayload, RegisterResponse>(
    AUTH_ENDPOINTS.REGISTER,
    payload
  );
}

/**
 * CONNEXION
 * Route : POST /api/v1/auth/login
 *
 * Envoie email + password. Si le compte a le 2FA activé,
 * le back-end retourne mfaRequired=true et un sessionToken.
 * Le front redirige alors vers /two-factor pour la vérification.
 *
 * Si 2FA désactivé → accessToken retourné directement.
 *
 * @param payload Email et mot de passe
 * @returns Réponse avec token(s) ou indication de 2FA requis
 */
export async function loginUser(
  payload: LoginPayload
): Promise<LoginResponse> {
  return postJson<LoginPayload, LoginResponse>(
    AUTH_ENDPOINTS.LOGIN,
    payload
  );
}

/**
 * VÉRIFICATION DU CODE 2FA
 * Route : POST /api/v1/auth/verify
 *
 * Envoie l'email et le code TOTP à 6 chiffres saisi par l'utilisateur.
 * Si correct → accessToken retourné → connexion complète.
 * Si incorrect → erreur levée.
 *
 * @param payload Email de l'utilisateur + code OTP à 6 chiffres
 * @returns Réponse avec accessToken si succès
 */
export async function verifyTwoFactor(
  payload: VerifyPayload
): Promise<VerifyResponse> {
  return postJson<VerifyPayload, VerifyResponse>(
    AUTH_ENDPOINTS.VERIFY_2FA,
    payload
  );
}

/**
 * MOT DE PASSE OUBLIÉ
 * Route : POST /api/v1/auth/forgot-password
 *
 * Envoie un email de réinitialisation.
 * Par sécurité, toujours afficher un message de succès côté UI
 * même si l'email n'existe pas (évite l'énumération de comptes).
 *
 * @param payload Email de l'utilisateur
 */
export async function forgotPassword(
  payload: ForgotPasswordPayload
): Promise<GenericResponse> {
  return postJson<ForgotPasswordPayload, GenericResponse>(
    // Endpoint à ajouter dans constants/auth.ts si disponible
    `https://gbe-8clf.onrender.com/api/v1/auth/forgot-password`,
    payload
  );
}

// ─────────────────────────────────────────────────────────────
// GESTION DU TOKEN EN SESSION (localStorage)
// ─────────────────────────────────────────────────────────────

/**
 * Sauvegarde le JWT d'accès dans localStorage.
 * Appelé après une connexion réussie (avec ou sans 2FA).
 */
export function saveAccessToken(token: string): void {
  if (typeof window !== 'undefined') {
    localStorage.setItem('gbe_access_token', token);
  }
}

/**
 * Récupère le JWT d'accès depuis localStorage.
 * Utilisé pour les requêtes aux routes protégées.
 */
export function getAccessToken(): string | null {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('gbe_access_token');
  }
  return null;
}

/**
 * Supprime le JWT d'accès (déconnexion).
 */
export function clearTokens(): void {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('gbe_access_token');
    sessionStorage.removeItem('gbe_email_2fa');
  }
}