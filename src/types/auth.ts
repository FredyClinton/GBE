// ============================================================
// FICHIER  : src/types/auth.ts
// RÔLE     : Interfaces TypeScript pour l'API back-end GBE.
// ============================================================

// ── Payloads (ce qu'on ENVOIE) ──────────────────────────────

export interface LoginPayload {
  email:    string;
  password: string;
}

export interface RegisterPayload {
  firstName:       string;
  lastName:        string;
  email:           string;
  password:        string;
  confirmPassword: string;
  phoneNumber:     string;
  dateOfBirth:     string;   // "1990-01-25"
  mfaEnabled:      boolean;  // Toujours true
}

export interface VerifyPayload {
  email: string;   // Email de l'utilisateur
  code:  string;   // Code TOTP à 6 chiffres
}

export interface ForgotPasswordPayload {
  email: string;
}

// ── Réponses (ce que le back-end RETOURNE) ──────────────────

/**
 * Réponse après inscription.
 * Le back-end retourne :
 *   {
 *     "mfaEnabled": true,
 *     "secretImageUri": "data:image/png;base64,..."
 *   }
 */
export interface RegisterResponse {
  mfaEnabled?:    boolean;
  secretImageUri?: string;  // ✅ Vrai nom du champ retourné par le back-end
  // Champs alternatifs au cas où la structure change
  qrCodeUrl?:     string;
  qrCode?:        string;
  message?:       string;
}

/**
 * Réponse après connexion.
 * Si 2FA activé → mfaRequired=true, pas d'accessToken.
 * Si 2FA désactivé → accessToken direct.
 */
export interface LoginResponse {
  success?:       boolean;
  message?:       string;
  accessToken?:   string;
  refreshToken?:  string;
  mfaRequired?:   boolean;
  email?:         string;
}

/**
 * Réponse après vérification du code 2FA.
 */
export interface VerifyResponse {
  success?:      boolean;
  message?:      string;
  accessToken?:  string;
  refreshToken?: string;
  user?: {
    id:        string;
    firstName: string;
    lastName:  string;
    email:     string;
    role:      string;
  };
}

export interface GenericResponse {
  success?: boolean;
  message?: string;
}

// Dictionnaire d'erreurs de validation par champ
export type FormErrors = Record<string, string | undefined>;