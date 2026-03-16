'use client';

// ============================================================
// FICHIER  : src/app/login/page.tsx
// RÔLE     : Page de connexion reliée au back-end GBE.
//
// FLUX :
//   1. Utilisateur saisit email + mot de passe
//   2. Appel POST /api/v1/auth/login
//   3. Back-end répond :
//      a) mfaRequired=true  → stocker l'email → rediriger vers /two-factor
//      b) accessToken direct → stocker le token → rediriger vers /dashboard
//
// NOTE : Le champ "Matricule" a été retiré car l'API n'en a pas
//        besoin pour le login. Il reste disponible dans le formulaire
//        d'inscription (register).
// ============================================================

import React, { useState } from 'react';
import Link          from 'next/link';
import { useRouter } from 'next/navigation';
import AuthLayout    from '@/components/auth/AuthLayout';
import Input         from '@/components/ui/Input';
import Button        from '@/components/ui/Button';
import { loginUser, saveAccessToken } from '@/lib/authService';
import { LoginPayload, FormErrors }   from '@/types/auth';
import { APP_ROUTES }                 from '@/constants/auth';

// ── Icônes SVG inline (pas de dépendance externe) ──────────
const IconMail = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="2" y="4" width="20" height="16" rx="2"/>
    <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
  </svg>
);
const IconLock = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="3" y="11" width="18" height="11" rx="2"/>
    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
  </svg>
);
const IconEye = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
    <circle cx="12" cy="12" r="3"/>
  </svg>
);
const IconEyeOff = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);
const IconShield = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
  </svg>
);
const IconAlert = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="alert__icon">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="8" x2="12" y2="12"/>
    <line x1="12" y1="16" x2="12.01" y2="16"/>
  </svg>
);

// ─────────────────────────────────────────────────────────────
// Validation locale (avant d'appeler l'API)
// Permet d'éviter des appels réseau inutiles pour des saisies
// manifestement incorrectes.
// ─────────────────────────────────────────────────────────────
function validate(values: LoginPayload): FormErrors {
  const errors: FormErrors = {};

  if (!values.email.trim()) {
    errors.email = "L'adresse email est requise";
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
    errors.email = 'Adresse email invalide';
  }

  if (!values.password) {
    errors.password = 'Le mot de passe est requis';
  }

  return errors;
}

// ─────────────────────────────────────────────────────────────
// COMPOSANT PAGE
// ─────────────────────────────────────────────────────────────
export default function LoginPage() {
  const router = useRouter();

  // ── État local du formulaire ──
  const [values, setValues] = useState<LoginPayload>({
    email:    '',
    password: '',
  });
  const [fieldErrors, setFieldErrors] = useState<FormErrors>({});
  const [apiError,    setApiError]    = useState('');
  const [isLoading,   setIsLoading]   = useState(false);
  const [showPwd,     setShowPwd]     = useState(false);

  // ── Mise à jour d'un champ : efface l'erreur du champ modifié ──
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setValues(prev => ({ ...prev, [name]: value }));
    // Effacer l'erreur de ce champ dès que l'utilisateur retape
    if (fieldErrors[name]) {
      setFieldErrors(prev => ({ ...prev, [name]: undefined }));
    }
    setApiError('');
  };

  // ── Soumission du formulaire ──
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Étape 1 : Validation locale
    const errors = validate(values);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return; // Arrêter ici — pas besoin d'appeler le back-end
    }

    // Étape 2 : Appel API
    setIsLoading(true);
    setApiError('');

    try {
      const response = await loginUser(values);

      if (response.mfaRequired) {
        // ✅ Cas A : Le compte a le 2FA activé
        // → Stocker l'email en sessionStorage pour la page de vérification
        //   (sessionStorage est effacé à la fermeture de l'onglet)
        sessionStorage.setItem('gbe_email_2fa', values.email);
        router.push(APP_ROUTES.TWO_FACTOR);

      } else if (response.accessToken) {
        // ✅ Cas B : Connexion directe sans 2FA
        // → Stocker le JWT dans localStorage pour les futures requêtes
        saveAccessToken(response.accessToken);
        router.push(APP_ROUTES.DASHBOARD);

      } else {
        // ⚠️ Cas inattendu : ni token ni 2FA requis
        setApiError(response.message || 'Réponse inattendue du serveur.');
      }

    } catch (err) {
      // Erreur réseau ou erreur HTTP retournée par le back-end
      setApiError(
        err instanceof Error
          ? err.message
          : 'Une erreur est survenue. Veuillez réessayer.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  // ── Rendu ──
  return (
    <AuthLayout
      title="Connexion"
      subtitle="Accédez à votre espace de gestion budgétaire"
    >
      {/* Alerte d'erreur globale (retournée par l'API) */}
      {apiError && (
        <div className="alert alert--error" role="alert">
          <IconAlert />
          {apiError}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>

          {/* ── Email ── */}
          <Input
            id="email"
            name="email"
            type="email"
            label="Adresse email"
            placeholder="vous@exemple.cm"
            value={values.email}
            onChange={handleChange}
            error={fieldErrors.email}
            icon={<IconMail />}
            autoComplete="email"
            autoFocus
            disabled={isLoading}
          />

          {/* ── Mot de passe + lien oublié ── */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <Input
              id="password"
              name="password"
              type={showPwd ? 'text' : 'password'}
              label="Mot de passe"
              placeholder="••••••••"
              value={values.password}
              onChange={handleChange}
              error={fieldErrors.password}
              icon={<IconLock />}
              autoComplete="current-password"
              disabled={isLoading}
              rightElement={
                <button
                  type="button"
                  className="pwd-toggle"
                  onClick={() => setShowPwd(v => !v)}
                  aria-label={showPwd ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                >
                  {showPwd ? <IconEyeOff /> : <IconEye />}
                </button>
              }
            />
            {/* Lien mot de passe oublié */}
            <Link href={APP_ROUTES.FORGOT_PASSWORD} className="forgot-link">
              Mot de passe oublié ?
            </Link>
          </div>

          {/* ── Bouton de connexion ── */}
          <Button type="submit" isLoading={isLoading} fullWidth>
            Se connecter
          </Button>

          {/* Badge sécurité */}
          <div className="secure-badge">
            <IconShield />
            Connexion sécurisée – MINFI Cameroun
          </div>

        </div>
      </form>

      {/* Lien vers l'inscription */}
      <p className="auth-switch">
        Pas encore de compte ?{' '}
        <Link href={APP_ROUTES.REGISTER}>Créer un compte</Link>
      </p>
    </AuthLayout>
  );
}