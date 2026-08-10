'use client';

// ============================================================
// FICHIER  : src/app/login/page.tsx
// RÔLE     : Page de connexion.
//            Champs  : email, matricule, mot de passe.
//            Extras  : toggle visibilité mdp, lien "oublié ?",
//                      redirection vers 2FA si activé.
// ============================================================

import React, { useState } from 'react';
import Link        from 'next/link';
import { useRouter } from 'next/navigation';
import AuthLayout  from '@/components/auth/AuthLayout';
import Input       from '@/components/ui/Input';
import Button      from '@/components/ui/Button';
import { loginUser } from '@/lib/authService';
import { storeSession } from '@/lib/session';
import { LoginPayload, FormErrors } from '@/types/auth';
import { APP_ROUTES }  from '@/constants/auth';

// ── Icônes SVG (inline, sans dépendance externe) ────────────
const IconMail = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2">
    <rect x="2" y="4" width="20" height="16" rx="2"/>
    <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
  </svg>
);
const IconLock = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2">
    <rect x="3" y="11" width="18" height="11" rx="2"/>
    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
  </svg>
);
const IconEye = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
    <circle cx="12" cy="12" r="3"/>
  </svg>
);
const IconEyeOff = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8
             a18.45 18.45 0 0 1 5.06-5.94"/>
    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8
             a18.5 18.5 0 0 1-2.16 3.19"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);
const IconShield = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2">
    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
  </svg>
);
const IconAlert = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2" className="alert__icon">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="8"  x2="12" y2="12"/>
    <line x1="12" y1="16" x2="12.01" y2="16"/>
  </svg>
);

// ─────────────────────────────────────────────────────────────
// Validation locale du formulaire de connexion
// ─────────────────────────────────────────────────────────────
function validate(values: LoginPayload): FormErrors {
  const e: FormErrors = {};

  if (!values.email.trim()) {
    e.email = "L'adresse email est requise";
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
    e.email = 'Adresse email invalide';
  }

  if (!values.password) {
    e.password = 'Le mot de passe est requis';
  }

  return e;
}

// ─────────────────────────────────────────────────────────────
// COMPOSANT PAGE
// ─────────────────────────────────────────────────────────────
export default function LoginPage() {
  const router = useRouter();

  // ── État du formulaire ──
  const [values, setValues] = useState<LoginPayload>({
    email:    '',
    password: '',
  });
  const [errors,      setErrors]      = useState<FormErrors>({});
  const [globalError, setGlobalError] = useState('');
  const [isLoading,   setIsLoading]   = useState(false);
  const [showPwd,     setShowPwd]     = useState(false);

  // ── Mise à jour d'un champ + effacement de son erreur ──
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setValues(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: undefined }));
    setGlobalError('');
  };

  // ── Soumission ──
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 1. Validation locale
    const fieldErrors = validate(values);
    if (Object.keys(fieldErrors).length > 0) {
      setErrors(fieldErrors);
      return;
    }

    // 2. Appel API
    setIsLoading(true);
    setGlobalError('');
    try {
      const res = await loginUser(values);

      if (res.accessToken) {
        // Connexion complète (MFA désactivé ou déjà vérifié)
        storeSession(res);
        router.push(APP_ROUTES.DASHBOARD);
      } else if (res.firstLogin && res.mfaToken) {
        // Première connexion : enrôlement MFA requis (scan du QR code)
        sessionStorage.setItem('gbe_mfa_token', res.mfaToken);
        sessionStorage.setItem('gbe_mfa_email', values.email);
        sessionStorage.setItem('gbe_mfa_qr', res.secretImageUri ?? '');
        router.push(APP_ROUTES.MFA_SETUP);
      } else if (res.mfaToken) {
        // MFA déjà activé : vérification du code TOTP
        sessionStorage.setItem('gbe_mfa_token', res.mfaToken);
        sessionStorage.setItem('gbe_mfa_email', values.email);
        router.push(APP_ROUTES.TWO_FACTOR);
      } else {
        setGlobalError('Identifiants incorrects. Veuillez réessayer.');
      }
    } catch (err) {
      setGlobalError(
        err instanceof Error ? err.message : 'Une erreur est survenue.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Connexion"
      subtitle="Accédez à votre espace de gestion budgétaire"
    >
      {/* ── Erreur globale API ── */}
      {globalError && (
        <div className="alert alert--error" role="alert">
          <IconAlert />
          {globalError}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>

          {/* Email */}
          <Input
            id="email" name="email" type="email"
            label="Adresse email"
            placeholder="vous@exemple.cm"
            value={values.email}
            onChange={handleChange}
            error={errors.email}
            icon={<IconMail />}
            autoComplete="email"
            autoFocus
            disabled={isLoading}
          />

          {/* Mot de passe + lien oublié */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <Input
              id="password" name="password"
              type={showPwd ? 'text' : 'password'}
              label="Mot de passe"
              placeholder="••••••••"
              value={values.password}
              onChange={handleChange}
              error={errors.password}
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

          {/* Bouton connexion */}
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
    </AuthLayout>
  );
}