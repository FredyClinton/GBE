'use client';

// ============================================================
// FICHIER  : src/app/two-factor/page.tsx
// RÔLE     : Page de vérification 2FA.
//            Saisie case par case d'un code OTP à 6 chiffres.
//            Fonctionnalités : auto-focus, collage, minuterie,
//                              renvoi de code, animation erreur.
// ============================================================

import React, { useState, useRef, useEffect, useCallback } from 'react';
import Link        from 'next/link';
import { useRouter } from 'next/navigation';
import AuthLayout  from '@/components/auth/AuthLayout';
import Button      from '@/components/ui/Button';
import { verifyMfa } from '@/lib/authService';
import { storeSession } from '@/lib/session';
import {
  APP_ROUTES,
  TWO_FACTOR_CODE_LENGTH,
  TWO_FACTOR_CODE_EXPIRY_SECONDS,
} from '@/constants/auth';

// ── Formatage du minuteur en MM:SS ──
const formatTime = (s: number) =>
  `${String(Math.floor(s / 60)).padStart(2,'0')}:${String(s % 60).padStart(2,'0')}`;

// ── Icônes ──────────────────────────────────────────────────
const IconAlert = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2" className="alert__icon">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="8"  x2="12" y2="12"/>
    <line x1="12" y1="16" x2="12.01" y2="16"/>
  </svg>
);
const IconArrowLeft = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2">
    <line x1="19" y1="12" x2="5" y2="12"/>
    <polyline points="12,19 5,12 12,5"/>
  </svg>
);

// ─────────────────────────────────────────────────────────────
// COMPOSANT PAGE
// ─────────────────────────────────────────────────────────────
export default function TwoFactorPage() {
  const router = useRouter();

  // Tableau d'une chaîne par case OTP
  const [otp,          setOtp]          = useState<string[]>(
    Array(TWO_FACTOR_CODE_LENGTH).fill('')
  );
  const [hasError,     setHasError]     = useState(false);
  const [errorMsg,     setErrorMsg]     = useState('');
  const [isLoading,    setIsLoading]    = useState(false);
  const [timeLeft,     setTimeLeft]     = useState(TWO_FACTOR_CODE_EXPIRY_SECONDS);

  // Références sur chaque input pour le focus automatique
  const refs = useRef<Array<HTMLInputElement | null>>(
    Array(TWO_FACTOR_CODE_LENGTH).fill(null)
  );

  // ── Minuterie ──
  useEffect(() => {
    if (timeLeft <= 0) return;
    const t = setInterval(() => setTimeLeft(n => n - 1), 1000);
    return () => clearInterval(t);
  }, [timeLeft]);

  const isExpired   = timeLeft <= 0;
  const codeIsFull  = otp.every(c => c !== '');
  const otpValue    = otp.join('');

  // Récupérer le mfaToken et l'email stockés après le login
  const getMfaToken = () => sessionStorage.getItem('gbe_mfa_token') ?? '';
  const getEmail     = () => sessionStorage.getItem('gbe_mfa_email') ?? '';

  // ── Gestion de la saisie dans une case ──
  const handleInput = useCallback((index: number, raw: string) => {
    // N'accepter que les chiffres
    if (!/^\d*$/.test(raw)) return;
    setHasError(false);
    setErrorMsg('');

    const next = [...otp];

    if (raw.length > 1) {
      // Cas collage : distribuer les chiffres sur les cases
      const digits = raw.replace(/\D/g,'').slice(0, TWO_FACTOR_CODE_LENGTH);
      for (let i = 0; i < TWO_FACTOR_CODE_LENGTH; i++) {
        next[i] = digits[i] ?? '';
      }
      setOtp(next);
      refs.current[Math.min(digits.length, TWO_FACTOR_CODE_LENGTH - 1)]?.focus();
      return;
    }

    next[index] = raw;
    setOtp(next);
    // Auto-focus case suivante
    if (raw && index < TWO_FACTOR_CODE_LENGTH - 1) {
      refs.current[index + 1]?.focus();
    }
  }, [otp]);

  // ── Touches spéciales : Backspace, flèches ──
  const handleKeyDown = useCallback((index: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace') {
      if (otp[index]) {
        const next = [...otp]; next[index] = ''; setOtp(next);
      } else if (index > 0) {
        refs.current[index - 1]?.focus();
      }
    } else if (e.key === 'ArrowLeft'  && index > 0) {
      refs.current[index - 1]?.focus();
    } else if (e.key === 'ArrowRight' && index < TWO_FACTOR_CODE_LENGTH - 1) {
      refs.current[index + 1]?.focus();
    }
  }, [otp]);

  // ── Vérification du code ──
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!codeIsFull || isExpired) return;

    setIsLoading(true);
    setHasError(false);
    setErrorMsg('');
    try {
      const res = await verifyMfa({
        email:    getEmail(),
        code:     otpValue,
        mfaToken: getMfaToken(),
      });

      if (res.accessToken) {
        sessionStorage.removeItem('gbe_mfa_token');
        sessionStorage.removeItem('gbe_mfa_email');
        storeSession(res);
        router.push(APP_ROUTES.DASHBOARD);
      } else {
        setHasError(true);
        setErrorMsg('Code incorrect. Veuillez réessayer.');
      }
    } catch (err) {
      setHasError(true);
      setErrorMsg(err instanceof Error ? err.message : 'Code invalide ou expiré.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Vérification en deux étapes"
      subtitle="Saisissez le code à 6 chiffres généré par votre application d'authentification"
    >
      {/* Erreur */}
      {errorMsg && (
        <div className="alert alert--error" role="alert">
          <IconAlert /> {errorMsg}
        </div>
      )}

      {/* Code expiré */}
      {isExpired && !errorMsg && (
        <div className="alert alert--error" role="alert">
          <IconAlert /> Le code a expiré. Veuillez en demander un nouveau.
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

          {/* Cases OTP */}
          <div>
            <p style={{
              fontSize: '.8125rem', fontWeight: 600,
              color: 'var(--clr-gray-600)', marginBottom: '12px',
            }}>
              Code à {TWO_FACTOR_CODE_LENGTH} chiffres
            </p>

            <div className="otp-container"
              role="group"
              aria-label={`Code de vérification à ${TWO_FACTOR_CODE_LENGTH} chiffres`}>
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
                  disabled={isLoading || isExpired}
                  aria-label={`Chiffre ${i + 1}`}
                  className={[
                    'otp-input',
                    digit              ? 'otp-input--filled' : '',
                    hasError           ? 'otp-input--error'  : '',
                  ].join(' ')}
                />
              ))}
            </div>

            {/* Minuterie */}
            <p className={`otp-timer ${isExpired ? 'otp-timer--expired' : ''}`}>
              {isExpired
                ? <>Code <strong>expiré</strong></>
                : <>Expire dans <strong>{formatTime(timeLeft)}</strong></>
              }
            </p>
          </div>

          {/* Bouton vérifier */}
          <Button
            type="submit"
            isLoading={isLoading}
            fullWidth
            disabled={!codeIsFull || isExpired || isLoading}
          >
            Vérifier le code
          </Button>

          {/* Retour connexion */}
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