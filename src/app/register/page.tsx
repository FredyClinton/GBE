'use client';

// ============================================================
// FICHIER  : src/app/register/page.tsx
// RÔLE     : Page d'inscription reliée au back-end GBE.
//
// FLUX :
//   1. Utilisateur remplit le formulaire
//   2. POST /api/v1/auth/register { ...données, mfaEnabled: true }
//   3. Back-end retourne :
//      { "mfaEnabled": true, "secretImageUri": "data:image/png;base64,..." }
//   4. On stocke secretImageUri dans sessionStorage
//   5. Redirection vers /register/qrcode pour afficher le QR code
// ============================================================

import React, { useState } from 'react';
import Link          from 'next/link';
import { useRouter } from 'next/navigation';
import AuthLayout    from '@/components/auth/AuthLayout';
import Input         from '@/components/ui/Input';
import Button        from '@/components/ui/Button';
import { APP_ROUTES } from '@/constants/auth';
import { RegisterPayload, FormErrors } from '@/types/auth';

// ── Icônes SVG inline ───────────────────────────────────────
const IconUser = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
  </svg>
);
const IconMail = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="2" y="4" width="20" height="16" rx="2"/>
    <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
  </svg>
);
const IconPhone = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 13.5a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 2.69h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 10.09a16 16 0 0 0 6 6l.91-.91a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 17z"/>
  </svg>
);
const IconCalendar = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="3" y="4" width="18" height="18" rx="2"/>
    <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
    <line x1="3" y1="10" x2="21" y2="10"/>
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
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
  </svg>
);
const IconEyeOff = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);
const IconAlert = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="alert__icon">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
  </svg>
);

// ─────────────────────────────────────────────────────────────
// Force du mot de passe
// ─────────────────────────────────────────────────────────────
function passwordStrength(pwd: string): 0 | 1 | 2 | 3 {
  if (!pwd) return 0;
  let score = 0;
  if (pwd.length >= 8) score++;
  if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) score++;
  if (/[0-9]/.test(pwd) && /[^A-Za-z0-9]/.test(pwd)) score++;
  return score as 0 | 1 | 2 | 3;
}
const STRENGTH_LABELS = ['', 'Faible', 'Moyen', 'Fort']    as const;
const STRENGTH_MODS   = ['', 'weak',   'medium', 'strong'] as const;
const STRENGTH_COLORS = ['', 'var(--clr-red)', 'var(--clr-yellow-dark)', 'var(--clr-green)'] as const;

// ─────────────────────────────────────────────────────────────
// Validation locale
// ─────────────────────────────────────────────────────────────
function validate(v: RegisterPayload): FormErrors {
  const e: FormErrors = {};
  if (!v.firstName.trim())  e.firstName = 'Le prénom est requis';
  if (!v.lastName.trim())   e.lastName  = 'Le nom est requis';
  if (!v.email.trim()) {
    e.email = "L'adresse email est requise";
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.email)) {
    e.email = 'Adresse email invalide';
  }
  if (!v.phoneNumber.trim()) {
    e.phoneNumber = 'Le numéro de téléphone est requis';
  } else if (!/^\+?[0-9\s\-]{8,15}$/.test(v.phoneNumber)) {
    e.phoneNumber = 'Format attendu : +237 6XX XXX XXX';
  }
  if (!v.dateOfBirth) {
    e.dateOfBirth = 'La date de naissance est requise';
  }
  if (!v.password) {
    e.password = 'Le mot de passe est requis';
  } else if (v.password.length < 8) {
    e.password = 'Minimum 8 caractères';
  } else if (!/(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z\d])/.test(v.password)) {
    e.password = 'Requiert majuscule, minuscule, chiffre et caractère spécial';
  }
  if (!v.confirmPassword) {
    e.confirmPassword = 'Veuillez confirmer votre mot de passe';
  } else if (v.password !== v.confirmPassword) {
    e.confirmPassword = 'Les mots de passe ne correspondent pas';
  }
  return e;
}

// ─────────────────────────────────────────────────────────────
// COMPOSANT PAGE
// ─────────────────────────────────────────────────────────────
export default function RegisterPage() {
  const router = useRouter();

  const [values, setValues] = useState<RegisterPayload>({
    firstName: '', lastName: '', email: '', password: '',
    confirmPassword: '', phoneNumber: '', dateOfBirth: '',
    mfaEnabled: true,  // Toujours true — l'application impose le 2FA
  });
  const [fieldErrors, setFieldErrors] = useState<FormErrors>({});
  const [apiError,    setApiError]    = useState('');
  const [isLoading,   setIsLoading]   = useState(false);
  const [showPwd,     setShowPwd]     = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const strength = passwordStrength(values.password);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setValues(prev => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) setFieldErrors(prev => ({ ...prev, [name]: undefined }));
    setApiError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validation locale
    const errors = validate(values);
    if (Object.keys(errors).length > 0) { setFieldErrors(errors); return; }

    setIsLoading(true);
    setApiError('');

    try {
      // Appel API POST /api/v1/auth/register
      const res = await fetch('https://gbe-8clf.onrender.com/api/v1/auth/register', {
        method:  'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body:    JSON.stringify(values),
      });

      const data = await res.json();

      if (!res.ok) {
        // Erreur HTTP (400, 409 email déjà utilisé, etc.)
        throw new Error(data.message || `Erreur ${res.status}`);
      }

      // ✅ Le back-end retourne { mfaEnabled: true, secretImageUri: "data:image/png;base64,..." }
      const qrImage = data.secretImageUri;

      if (qrImage) {
        // Stocker le QR code et l'email pour la page suivante
        // sessionStorage est effacé à la fermeture de l'onglet (sécurité)
        sessionStorage.setItem('gbe_qr_code',   qrImage);
        sessionStorage.setItem('gbe_email_2fa', values.email);

        // Rediriger vers la page de scan du QR code
        router.push(APP_ROUTES.REGISTER_QR);
      } else {
        // Inscription réussie mais pas de QR code (ne devrait pas arriver)
        setApiError("Inscription réussie mais aucun QR code reçu. Contactez l'administrateur.");
      }

    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Une erreur est survenue.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Créer un compte"
      subtitle="Renseignez vos informations pour accéder à la plateforme GBE"
    >
      {/* Erreur API */}
      {apiError && (
        <div className="alert alert--error" role="alert">
          <IconAlert /> {apiError}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>

          {/* Prénom / Nom */}
          <div className="form-grid-2">
            <Input id="firstName" name="firstName" type="text"
              label="Prénom" placeholder="Ali"
              value={values.firstName} onChange={handleChange}
              error={fieldErrors.firstName} icon={<IconUser />}
              autoComplete="given-name" disabled={isLoading} autoFocus />
            <Input id="lastName" name="lastName" type="text"
              label="Nom" placeholder="Bello"
              value={values.lastName} onChange={handleChange}
              error={fieldErrors.lastName} icon={<IconUser />}
              autoComplete="family-name" disabled={isLoading} />
          </div>

          {/* Email */}
          <Input id="email" name="email" type="email"
            label="Adresse email" placeholder="vous@exemple.cm"
            value={values.email} onChange={handleChange}
            error={fieldErrors.email} icon={<IconMail />}
            autoComplete="email" disabled={isLoading} />

          {/* Téléphone */}
          <Input id="phoneNumber" name="phoneNumber" type="tel"
            label="Numéro de téléphone" placeholder="+237 6XX XXX XXX"
            value={values.phoneNumber} onChange={handleChange}
            error={fieldErrors.phoneNumber} icon={<IconPhone />}
            autoComplete="tel" disabled={isLoading} />

          {/* Date de naissance */}
          <Input id="dateOfBirth" name="dateOfBirth" type="date"
            label="Date de naissance"
            value={values.dateOfBirth} onChange={handleChange}
            error={fieldErrors.dateOfBirth} icon={<IconCalendar />}
            autoComplete="bday" disabled={isLoading} />

          {/* Mot de passe + force */}
          <div>
            <Input id="password" name="password"
              type={showPwd ? 'text' : 'password'}
              label="Mot de passe" placeholder="••••••••"
              value={values.password} onChange={handleChange}
              error={fieldErrors.password} icon={<IconLock />}
              autoComplete="new-password" disabled={isLoading}
              rightElement={
                <button type="button" className="pwd-toggle"
                  onClick={() => setShowPwd(v => !v)}
                  aria-label={showPwd ? 'Masquer' : 'Afficher'}>
                  {showPwd ? <IconEyeOff /> : <IconEye />}
                </button>
              }
            />
            {values.password && (
              <div style={{ marginTop: 6 }}>
                <div className="pwd-strength">
                  {[1,2,3].map(i => (
                    <div key={i} className={`pwd-strength__bar ${strength >= i ? `pwd-strength__bar--${STRENGTH_MODS[strength]}` : ''}`} />
                  ))}
                </div>
                <p className="pwd-strength__label">
                  Force : <strong style={{ color: STRENGTH_COLORS[strength] }}>{STRENGTH_LABELS[strength]}</strong>
                </p>
              </div>
            )}
          </div>

          {/* Confirmation mot de passe */}
          <Input id="confirmPassword" name="confirmPassword"
            type={showConfirm ? 'text' : 'password'}
            label="Confirmer le mot de passe" placeholder="••••••••"
            value={values.confirmPassword} onChange={handleChange}
            error={fieldErrors.confirmPassword} icon={<IconLock />}
            autoComplete="new-password" disabled={isLoading}
            rightElement={
              <button type="button" className="pwd-toggle"
                onClick={() => setShowConfirm(v => !v)}
                aria-label={showConfirm ? 'Masquer' : 'Afficher'}>
                {showConfirm ? <IconEyeOff /> : <IconEye />}
              </button>
            }
          />

          {/* Info 2FA */}
          <div className="alert alert--info" style={{ fontSize: '.8rem' }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="alert__icon">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            Un QR code vous sera fourni pour configurer l&apos;authentification à deux facteurs.
          </div>

          {/* Bouton */}
          <Button type="submit" variant="secondary" isLoading={isLoading} fullWidth>
            Créer mon compte
          </Button>

        </div>
      </form>

      <p className="auth-switch">
        Déjà inscrit ?{' '}
        <Link href={APP_ROUTES.LOGIN}>Se connecter</Link>
      </p>
    </AuthLayout>
  );
}