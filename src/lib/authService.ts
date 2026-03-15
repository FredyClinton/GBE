// ============================================================
// FICHIER  : src/lib/authService.ts
// RÔLE     : Couche d'abstraction pour tous les appels API
//            d'authentification. Les composants ne font jamais
//            de fetch directement — ils passent par ce service.
//            Pour brancher un vrai back-end : modifier uniquement
//            les endpoints dans src/constants/auth.ts.
// ============================================================

import {
  LoginPayload,
  RegisterPayload,
  TwoFactorPayload,
  ForgotPasswordPayload,
  AuthApiResponse,
} from '@/types/auth';
import { AUTH_ENDPOINTS } from '@/constants/auth';

// ── Headers par défaut pour toutes les requêtes ──
const DEFAULT_HEADERS: HeadersInit = {
  'Content-Type': 'application/json',
  'Accept':       'application/json',
};

/**
 * Utilitaire interne : exécute un POST et lève une exception
 * si la réponse HTTP n'est pas dans la plage 2xx.
 *
 * @param url     - URL de l'endpoint
 * @param payload - Corps de la requête (sera sérialisé en JSON)
 * @returns Réponse JSON typée AuthApiResponse
 */
async function postJson<T>(url: string, payload: T): Promise<AuthApiResponse> {
  const response = await fetch(url, {
    method:  'POST',
    headers: DEFAULT_HEADERS,
    body:    JSON.stringify(payload),
  });

  // Lire le corps même en cas d'erreur pour récupérer le message du serveur
  const data: AuthApiResponse = await response.json().catch(() => ({
    success: false,
    message: 'Erreur réseau ou réponse invalide du serveur.',
  }));

  if (!response.ok) {
    // Lever une erreur avec le message renvoyé par l'API
    throw new Error(data.message || `Erreur HTTP ${response.status}`);
  }

  return data;
}

// ─────────────────────────────────────────────────────────────
// FONCTIONS PUBLIQUES
// ─────────────────────────────────────────────────────────────

/**
 * Connecte un utilisateur avec email, matricule et mot de passe.
 * Si le 2FA est activé, la réponse contient un `sessionToken`
 * et non un `accessToken`.
 */
export async function loginUser(
  payload: LoginPayload
): Promise<AuthApiResponse> {
  return postJson(AUTH_ENDPOINTS.LOGIN, payload);
}

/**
 * Inscrit un nouvel utilisateur.
 * Le back-end doit valider l'unicité de l'email et du matricule.
 */
export async function registerUser(
  payload: RegisterPayload
): Promise<AuthApiResponse> {
  return postJson(AUTH_ENDPOINTS.REGISTER, payload);
}

/**
 * Vérifie le code OTP reçu par email ou SMS.
 * Retourne un `accessToken` si le code est correct.
 */
export async function verifyTwoFactor(
  payload: TwoFactorPayload
): Promise<AuthApiResponse> {
  return postJson(AUTH_ENDPOINTS.TWO_FACTOR_VERIFY, payload);
}

/**
 * Demande le renvoi d'un nouveau code OTP.
 * Nécessite le `sessionToken` de la session en cours.
 */
export async function resendTwoFactorCode(
  sessionToken: string
): Promise<AuthApiResponse> {
  return postJson(AUTH_ENDPOINTS.TWO_FACTOR_RESEND, { sessionToken });
}

/**
 * Envoie un email de réinitialisation de mot de passe.
 * Toujours retourner un succès côté UI (sécurité : ne pas
 * révéler si l'email existe dans la base).
 */
export async function forgotPassword(
  payload: ForgotPasswordPayload
): Promise<AuthApiResponse> {
  return postJson(AUTH_ENDPOINTS.FORGOT_PASSWORD, payload);
}