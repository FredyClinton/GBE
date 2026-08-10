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
  VerifyPayload,
  SetupMfaPayload,
  AuthenticationResponse,
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
 * @returns Réponse JSON typée AuthenticationResponse
 */
async function postJson<T>(url: string, payload: T): Promise<AuthenticationResponse> {
  const response = await fetch(url, {
    method:  'POST',
    headers: DEFAULT_HEADERS,
    body:    JSON.stringify(payload),
  });

  // Lire le corps même en cas d'erreur pour récupérer le message du serveur
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const message =
      (data && typeof data === 'object' && 'message' in data && String(data.message)) ||
      `Erreur HTTP ${response.status}`;
    throw new Error(message);
  }

  return data as AuthenticationResponse;
}

// ─────────────────────────────────────────────────────────────
// FONCTIONS PUBLIQUES
// ─────────────────────────────────────────────────────────────

/**
 * Connecte un utilisateur avec email et mot de passe.
 * Selon la réponse :
 * - `accessToken` présent      → connexion complète.
 * - `firstLogin` + `mfaToken`  → enrôlement MFA requis (/auth/setup-mfa).
 * - `mfaToken` seul            → code TOTP à vérifier (/auth/verify).
 */
export async function loginUser(
  payload: LoginPayload
): Promise<AuthenticationResponse> {
  return postJson(AUTH_ENDPOINTS.LOGIN, payload);
}

/**
 * Vérifie le code TOTP lors d'une connexion (MFA déjà activé).
 */
export async function verifyMfa(
  payload: VerifyPayload
): Promise<AuthenticationResponse> {
  return postJson(AUTH_ENDPOINTS.VERIFY, payload);
}

/**
 * Enrôle le MFA lors de la première connexion, après que
 * l'utilisateur a scanné le QR code (`secretImageUri`) et
 * saisi le code généré par son application TOTP.
 */
export async function setupMfa(
  payload: SetupMfaPayload
): Promise<AuthenticationResponse> {
  return postJson(AUTH_ENDPOINTS.SETUP_MFA, payload);
}

/**
 * Demande le renvoi d'un lien de réinitialisation de mot de passe.
 * ⚠️ Endpoint absent du openapi.json actuel du back-end — à activer
 * côté serveur avant que cette fonction puisse fonctionner.
 */
export async function forgotPassword(
  payload: { email: string }
): Promise<AuthenticationResponse> {
  return postJson(AUTH_ENDPOINTS.FORGOT_PASSWORD, payload);
}