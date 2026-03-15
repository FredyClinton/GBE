'use client';

// ============================================================
// FICHIER  : src/app/forgot-password/page.tsx
// RÔLE     : Page "Mot de passe oublié".
//            Saisie de l'email → envoi d'un lien de réinit.
//            Affiche un écran de confirmation après envoi.
// ============================================================

import React, { useState } from 'react';
import Link      from 'next/link';
import AuthLayout from '@/components/auth/AuthLayout';
import Input      from '@/components/ui/Input';
import Button     from '@/components/ui/Button';
import { forgotPassword } from '@/lib/authService';
import { APP_ROUTES } from '@/constants/auth';

// ── Icônes ──────────────────────────────────────────────────
const IconMail = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="2" y="4" width="20" height="16" rx="2"/>
    <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
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
const IconInfo = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2" className="alert__icon">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="16" x2="12" y2="12"/>
    <line x1="12" y1="8"  x2="12.01" y2="8"/>
  </svg>
);

// ─────────────────────────────────────────────────────────────
// COMPOSANT PAGE
// ─────────────────────────────────────────────────────────────
export default function ForgotPasswordPage() {
  const [email,       setEmail]       = useState('');
  const [emailError,  setEmailError]  = useState('');
  const [globalError, setGlobalError] = useState('');
  const [submitted,   setSubmitted]   = useState(false);
  const [isLoading,   setIsLoading]   = useState(false);

  // ── Validation locale de l'email ──
  const validateEmail = () => {
    if (!email.trim()) {
      setEmailError("L'adresse email est requise");
      return false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setEmailError('Adresse email invalide');
      return false;
    }
    return true;
  };

  // ── Soumission ──
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateEmail()) return;

    setIsLoading(true);
    setGlobalError('');
    try {
      await forgotPassword({ email });
      // Toujours afficher la confirmation, même si l'email
      // n'existe pas (sécurité : ne pas divulguer les comptes)
      setSubmitted(true);
    } catch (err) {
      setGlobalError(err instanceof Error ? err.message : 'Une erreur est survenue.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Mot de passe oublié"
      subtitle="Renseignez votre email pour recevoir un lien de réinitialisation"
    >
      {submitted ? (
        /* ── État : email envoyé ── */
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', textAlign: 'center' }}>

          {/* Icône succès */}
          <div style={{
            width: 72, height: 72, borderRadius: '50%',
            background: '#F0FDF4', border: '2px solid #BBF7D0',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '8px auto 0',
          }}>
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none"
              stroke="var(--clr-green)" strokeWidth="2">
              <rect x="2" y="4" width="20" height="16" rx="2"/>
              <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
            </svg>
          </div>

          {/* Message */}
          <div>
            <h3 style={{
              fontFamily: 'var(--font-display)',
              fontSize: '1.15rem',
              color: 'var(--clr-navy)',
              marginBottom: '8px',
            }}>
              Email envoyé !
            </h3>
            <p style={{ fontSize: '.875rem', color: 'var(--clr-gray-600)', lineHeight: 1.6 }}>
              Si un compte est associé à{' '}
              <strong style={{ color: 'var(--clr-navy)' }}>{email}</strong>,
              vous recevrez un lien de réinitialisation sous peu.
            </p>
          </div>

          {/* Conseil spam */}
          <div className="alert alert--info">
            <IconInfo />
            Vérifiez également vos dossiers spam et courrier indésirable.
          </div>

          {/* Retour */}
          <Link href={APP_ROUTES.LOGIN}>
            <Button variant="ghost" fullWidth>
              Retour à la connexion
            </Button>
          </Link>

        </div>
      ) : (
        /* ── État : formulaire ── */
        <>
          {globalError && (
            <div className="alert alert--error" role="alert">
              <IconAlert /> {globalError}
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>

              <Input
                id="email" name="email" type="email"
                label="Adresse email"
                placeholder="vous@exemple.cm"
                value={email}
                onChange={e => {
                  setEmail(e.target.value);
                  setEmailError('');
                  setGlobalError('');
                }}
                error={emailError}
                icon={<IconMail />}
                autoComplete="email"
                autoFocus
                disabled={isLoading}
              />

              <Button type="submit" isLoading={isLoading} fullWidth>
                Envoyer le lien
              </Button>

            </div>
          </form>

          <p className="auth-switch">
            <Link href={APP_ROUTES.LOGIN}>← Retour à la connexion</Link>
          </p>
        </>
      )}
    </AuthLayout>
  );
}