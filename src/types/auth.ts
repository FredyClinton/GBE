// ============================================================
// FICHIER  : src/types/auth.ts
// RÔLE     : Interfaces TypeScript alignées sur les schémas
//            OpenAPI réels de l'API GBE (tags "Authentication").
// ============================================================

/**
 * Payload envoyé au back-end lors de la connexion.
 * Route : POST /auth/login
 * Schéma back-end : AuthenticationRequest
 */
export interface LoginPayload {
  email:    string;
  password: string;
}

/**
 * Payload pour vérifier un code TOTP lors d'une connexion
 * ultérieure (MFA déjà activé).
 * Route : POST /auth/verify
 * Schéma back-end : VerificationRequest
 */
export interface VerifyPayload {
  email:    string;
  code:     string;
  mfaToken: string;
}

/**
 * Payload pour enrôler le MFA (première connexion) après avoir
 * scanné le QR code (`secretImageUri`) avec une app TOTP.
 * Route : POST /auth/setup-mfa
 * Schéma back-end : SetupMfaRequest
 */
export interface SetupMfaPayload {
  email:    string;
  code:     string;
  mfaToken: string;
}

/**
 * Contexte utilisateur renvoyé après authentification complète.
 * Schéma back-end : UserContext
 */
export interface UserContext {
  userId:    string;
  firstName: string;
  lastName:  string;
  email:     string;
  matricule: string;
  nui?:      string;
  cni?:      string;
  role:      string;
  mandats:   unknown[];
}

/**
 * Réponse standard des routes d'authentification.
 * Schéma back-end : AuthenticationResponse
 *
 * - `accessToken`/`refreshToken` présents  → authentification complète.
 * - `firstLogin: true` + `secretImageUri`  → enrôlement MFA requis (scan QR).
 * - `mfaToken` sans `accessToken`          → code TOTP à vérifier (/auth/verify).
 */
export interface AuthenticationResponse {
  accessToken?:    string;
  refreshToken?:   string;
  tokenType?:      string;
  mfaEnabled:      boolean;
  firstLogin:      boolean;
  secretImageUri?: string;
  mfaToken?:       string;
  userContext?:    UserContext;
}

/**
 * Dictionnaire d'erreurs de validation par champ.
 */
export type FormErrors = Record<string, string | undefined>;
