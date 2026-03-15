// ============================================================
// FICHIER  : src/types/auth.ts
// RÔLE     : Définition de toutes les interfaces TypeScript
//            utilisées dans les pages d'authentification.
//            Centraliser ici facilite la maintenance et
//            garantit la cohérence avec le back-end.
// ============================================================

/**
 * Payload envoyé au back-end lors de la connexion.
 * Route : POST /auth/login
 */
export interface LoginPayload {
  email:     string; // Adresse email de l'utilisateur
  matricule: string; // Identifiant administratif unique
  password:  string; // Mot de passe en clair (HTTPS obligatoire)
}

/**
 * Payload envoyé au back-end lors de l'inscription.
 * Route : POST /auth/register
 */
export interface RegisterPayload {
  firstName:       string; // Prénom
  lastName:        string; // Nom de famille
  email:           string; // Adresse email
  password:        string; // Mot de passe choisi
  confirmPassword: string; // Confirmation (validé côté client seulement)
  phoneNumber:     string; // Ex : +237655555555
  dateOfBirth:     string; // Format ISO : "1990-01-25"
  matricule:       string; // Matricule administratif
}

/**
 * Payload envoyé pour vérifier le code OTP 2FA.
 * Route : POST /auth/2fa/verify
 */
export interface TwoFactorPayload {
  code:         string; // Code à 6 chiffres reçu par email/SMS
  sessionToken: string; // Token temporaire retourné après login réussi
}

/**
 * Payload pour demander la réinitialisation du mot de passe.
 * Route : POST /auth/forgot-password
 */
export interface ForgotPasswordPayload {
  email: string;
}

/**
 * Structure de réponse standard de l'API d'authentification.
 * Toutes les routes auth renvoient ce format.
 */
export interface AuthApiResponse {
  success: boolean;
  message: string;
  data?: {
    accessToken?:  string; // JWT d'accès (connexion complète)
    refreshToken?: string; // JWT de rafraîchissement
    sessionToken?: string; // Token temporaire pour le flux 2FA
    user?: {
      id:        string;
      firstName: string;
      lastName:  string;
      email:     string;
      matricule: string;
      role:      string;
    };
  };
}

/**
 * Dictionnaire d'erreurs de validation par champ.
 * Clé = nom du champ, valeur = message d'erreur ou undefined.
 */
export type FormErrors = Record<string, string | undefined>;