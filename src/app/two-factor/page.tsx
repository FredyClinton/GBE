'use client';

// ============================================================
// FICHIER  : src/app/two-factor/page.tsx
// RÔLE     : Page de vérification du code TOTP (2FA).
//
// FLUX :
//   1. L'utilisateur arrive ici après une connexion réussie
//      qui a retourné mfaRequired=true
//   2. L'email de l'utilisateur a été stocké dans sessionStorage
//      par la page de login (clé : 'gbe_email_2fa')
//   3. L'utilisateur saisit le code à 6 chiffres généré par
//      son application TOTP (Google Authenticator, Authy…)
//   4. Appel POST /api/v1/auth/verify avec { email, code }
//   5. Si code correct → accessToken reçu → stocké → /dashboard
//   6. Si code incorrect → message d'erreur affiché
//
// SÉCURITÉ :
//   - Si aucun email en sessionStorage → redirection vers /login
//   - Le code OTP expire côté back-end (pas besoin de minuterie)
// ============================================================

import React, { useState, useRef, useEffect, useCallback } from 'react';
import Link          from 'next/link';
import { useRouter } from 'next/navigation';
import AuthLayout    from '@/components/auth/AuthLayout';
import Button        from '@/components/ui/Button';
import { verifyTwoFactor, saveAccessToken } from '@/lib/authService';
import { APP_ROUTES, TWO_FACTOR_CODE_LENGTH } from '@/constants/auth';

// ── Icônes ─────────────────────────────────────────────────
const IconAlert = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="alert__icon">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
  </svg>
);
const IconCheck = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="alert__icon">
    <polyline points="20,6 9,17 4,12"/>
  </svg>
);
const IconArrowLeft = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12,19 5,12 12,5"/>
  </svg>
);

// ─────────────────────────────────────────────────────────────
// COMPOSANT PAGE
// ─────────────────────────────────────────────────────────────
export default function TwoFactorPage() {
  const router = useRouter();

  // Tableau de 6 chaînes, une par case OTP
  const [otp, setOtp]         = useState<string[]>(Array(TWO_FACTOR_CODE_LENGTH).fill(''));
  const [hasError, setHasError] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [isLoading, setIsLoading]   = useState(false);
  // Email récupéré depuis sessionStorage (stocké par la page login)
  const [userEmail, setUserEmail]   = useState('');

  // Références sur les inputs pour le focus automatique
  const refs = useRef<Array<HTMLInputElement | null>>(
    Array(TWO_FACTOR_CODE_LENGTH).fill(null)
  );

  // ── Récupérer l'email depuis sessionStorage ──
  useEffect(() => {
    const email = sessionStorage.getItem('gbe_email_2fa');
    if (!email) {
      // Aucun email trouvé → l'utilisateur n'est pas passé par le login
      // → rediriger pour éviter une page inutilisable
      router.replace(APP_ROUTES.LOGIN);
      return;
    }
    setUserEmail(email);
  }, [router]);

  const codeIsFull = otp.every(c => c !== '');
  const otpValue   = otp.join('');

  // ── Gestion de la saisie case par case ──
  const handleInput = useCallback((index: number, raw: string) => {
    if (!/^\d*$/.test(raw)) return; // Accepter uniquement les chiffres

    setHasError(false);
    setErrorMsg('');

    const next = [...otp];

    // Cas collage : l'utilisateur colle un code entier (ex: "123456")
    if (raw.length > 1) {
      const digits = raw.replace(/\D/g, '').slice(0, TWO_FACTOR_CODE_LENGTH);
      for (let i = 0; i < TWO_FACTOR_CODE_LENGTH; i++) {
        next[i] = digits[i] ?? '';
      }
      setOtp(next);
      // Focus sur la dernière case remplie
      refs.current[Math.min(digits.length, TWO_FACTOR_CODE_LENGTH - 1)]?.focus();
      return;
    }

    // Saisie normale : un chiffre à la fois
    next[index] = raw;
    setOtp(next);
    // Auto-focus sur la case suivante après saisie
    if (raw && index < TWO_FACTOR_CODE_LENGTH - 1) {
      refs.current[index + 1]?.focus();
    }
  }, [otp]);

  // ── Gestion des touches spéciales ──
  const handleKeyDown = useCallback((index: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace') {
      if (otp[index]) {
        // Effacer la case courante
        const next = [...otp]; next[index] = ''; setOtp(next);
      } else if (index > 0) {
        // Reculer à la case précédente
        refs.current[index - 1]?.focus();
      }
    } else if (e.key === 'ArrowLeft'  && index > 0) {
      refs.current[index - 1]?.focus();
    } else if (e.key === 'ArrowRight' && index < TWO_FACTOR_CODE_LENGTH - 1) {
      refs.current[index + 1]?.focus();
    }
  }, [otp]);

  // ── Soumission du code ──
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!codeIsFull) return;

    setIsLoading(true);
    setHasError(false);
    setErrorMsg('');

    try {
      // Appel POST /api/v1/auth/verify avec email + code OTP
      const response = await verifyTwoFactor({
        email: userEmail,
        code:  otpValue,
      });

      if (response.accessToken) {
        // ✅ Code correct — sauvegarder le JWT
        saveAccessToken(response.accessToken);

        // Afficher un message de succès avant de rediriger
        setSuccessMsg('Code vérifié avec succès ! Connexion en cours…');

        // Nettoyage sessionStorage
        sessionStorage.removeItem('gbe_email_2fa');

        // Rediriger vers le tableau de bord après un court délai
        setTimeout(() => router.push(APP_ROUTES.DASHBOARD), 1200);

      } else {
        // Réponse inattendue du back-end
        setHasError(true);
        setErrorMsg(response.message || 'Code incorrect. Veuillez réessayer.');
      }

    } catch (err) {
      setHasError(true);
      setErrorMsg(
        err instanceof Error ? err.message : 'Code invalide ou expiré.'
      );
      // Secouer les cases visuellement (via classe CSS)
      // Réinitialiser les cases pour faciliter la nouvelle saisie
      setOtp(Array(TWO_FACTOR_CODE_LENGTH).fill(''));
      refs.current[0]?.focus();
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Vérification en deux étapes"
      subtitle="Saisissez le code à 6 chiffres de votre application d'authentification"
    >
      {/* Alerte d'erreur */}
      {errorMsg && (
        <div className="alert alert--error" role="alert">
          <IconAlert /> {errorMsg}
        </div>
      )}

      {/* Alerte de succès */}
      {successMsg && (
        <div className="alert alert--success" role="status">
          <IconCheck /> {successMsg}
        </div>
      )}

      {/* Affichage de l'email pour confirmation */}
      {userEmail && (
        <div className="alert alert--info" style={{ fontSize: '.8rem' }}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="alert__icon">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="16" x2="12" y2="12"/>
            <line x1="12" y1="8" x2="12.01" y2="8"/>
          </svg>
          Vérification pour : <strong>{userEmail}</strong>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

          {/* ── Cases OTP ── */}
          <div>
            <p style={{
              fontSize: '.8125rem', fontWeight: 600,
              color: 'var(--clr-gray-600)', marginBottom: '12px',
            }}>
              Code à {TWO_FACTOR_CODE_LENGTH} chiffres (Google Authenticator)
            </p>

            <div
              className="otp-container"
              role="group"
              aria-label={`Code de vérification à ${TWO_FACTOR_CODE_LENGTH} chiffres`}
            >
              {otp.map((digit, i) => (
                <input
                  key={i}
                  ref={el => { refs.current[i] = el; }}
                  type="text"
                  inputMode="numeric"
                  pattern="\d*"
                  maxLength={TWO_FACTOR_CODE_LENGTH}
                  value={digit}
                  onChange={e => handleInput(i, e.target.value)}
                  onKeyDown={e => handleKeyDown(i, e)}
                  onFocus={e => e.target.select()}
                  autoFocus={i === 0}
                  autoComplete={i === 0 ? 'one-time-code' : 'off'}
                  disabled={isLoading || !!successMsg}
                  aria-label={`Chiffre ${i + 1} du code`}
                  className={[
                    'otp-input',
                    digit      ? 'otp-input--filled' : '',
                    hasError   ? 'otp-input--error'  : '',
                  ].join(' ')}
                />
              ))}
            </div>
          </div>

          {/* Bouton vérifier */}
          <Button
            type="submit"
            isLoading={isLoading}
            fullWidth
            disabled={!codeIsFull || isLoading || !!successMsg}
          >
            Vérifier le code
          </Button>

          {/* Lien retour */}
          <div style={{
            textAlign: 'center',
            borderTop: '1px solid var(--clr-gray-100)',
            paddingTop: '14px',
          }}>
            <Link href={APP_ROUTES.LOGIN} style={{
              fontSize: '.8rem',
              color: 'var(--clr-gray-400)',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '5px',
            }}>
              <IconArrowLeft /> Retour à la connexion
            </Link>
          </div>

        </div>
      </form>
    </AuthLayout>
  );
}