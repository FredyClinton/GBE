'use client';

// ============================================================
// FICHIER  : src/app/register/page.tsx
// RÔLE     : Page d'inscription.
//            Champs  : prénom, nom, email, matricule,
//                      téléphone, date de naissance,
//                      mot de passe, confirmation mdp.
//            Extras  : indicateur force mdp, grille 2 colonnes.
// ============================================================

import React, { useState } from 'react';
import Link        from 'next/link';
import { useRouter } from 'next/navigation';
import AuthLayout  from '@/components/auth/AuthLayout';
import Input       from '@/components/ui/Input';
import Button      from '@/components/ui/Button';
import { registerUser } from '@/lib/authService';
import { RegisterPayload, FormErrors } from '@/types/auth';
import { APP_ROUTES } from '@/constants/auth';

// ── Icônes SVG ──────────────────────────────────────────────
const IconUser = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
    <circle cx="12" cy="7" r="4"/>
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
    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07
             A19.5 19.5 0 0 1 4.69 13.5a19.79 19.79 0 0 1-3.07-8.67
             A2 2 0 0 1 3.6 2.69h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81
             a2 2 0 0 1-.45 2.11L7.91 10.09a16 16 0 0 0 6 6l.91-.91
             a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 17z"/>
  </svg>
);
const IconCalendar = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="3" y="4" width="18" height="18" rx="2"/>
    <line x1="16" y1="2" x2="16" y2="6"/>
    <line x1="8"  y1="2" x2="8"  y2="6"/>
    <line x1="3"  y1="10" x2="21" y2="10"/>
  </svg>
);
const IconId = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="2" y="5" width="20" height="14" rx="2"/>
    <path d="M16 10h2M16 14h2M6 10h4v4H6z"/>
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
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8
             a18.45 18.45 0 0 1 5.06-5.94"/>
    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8
             a18.5 18.5 0 0 1-2.16 3.19"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);
const IconCheck = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2" className="alert__icon">
    <polyline points="20,6 9,17 4,12"/>
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
// Force du mot de passe (0 = vide, 1 = faible, 2 = moyen, 3 = fort)
// ─────────────────────────────────────────────────────────────
function passwordStrength(pwd: string): 0 | 1 | 2 | 3 {
  if (!pwd) return 0;
  let score = 0;
  if (pwd.length >= 8)                                        score++;
  if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd))               score++;
  if (/[0-9]/.test(pwd) && /[^A-Za-z0-9]/.test(pwd))        score++;
  return score as 0 | 1 | 2 | 3;
}

const STRENGTH_LABELS = ['', 'Faible', 'Moyen', 'Fort']        as const;
const STRENGTH_MODS   = ['', 'weak',   'medium', 'strong']     as const;
const STRENGTH_COLORS = ['', 'var(--clr-red)', 'var(--clr-yellow-dark)', 'var(--clr-green)'] as const;

// ─────────────────────────────────────────────────────────────
// Validation du formulaire d'inscription
// ─────────────────────────────────────────────────────────────
function validate(v: RegisterPayload): FormErrors {
  const e: FormErrors = {};

  if (!v.firstName.trim())   e.firstName = 'Le prénom est requis';
  if (!v.lastName.trim())    e.lastName  = 'Le nom est requis';

  if (!v.email.trim()) {
    e.email = "L'adresse email est requise";
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.email)) {
    e.email = 'Adresse email invalide';
  }

  if (!v.matricule.trim())   e.matricule = 'Le matricule est requis';

  if (!v.phoneNumber.trim()) {
    e.phoneNumber = 'Le numéro de téléphone est requis';
  } else if (!/^\+?[0-9\s\-]{8,15}$/.test(v.phoneNumber)) {
    e.phoneNumber = 'Format attendu : +237 6XX XXX XXX';
  }

  if (!v.dateOfBirth) {
    e.dateOfBirth = 'La date de naissance est requise';
  } else {
    const age = (Date.now() - new Date(v.dateOfBirth).getTime())
                / (365.25 * 24 * 3600 * 1000);
    if (age < 18) e.dateOfBirth = 'Vous devez avoir au moins 18 ans';
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
    firstName: '', lastName: '', email: '', matricule: '',
    phoneNumber: '', dateOfBirth: '', password: '', confirmPassword: '',
  });
  const [errors,      setErrors]      = useState<FormErrors>({});
  const [globalError, setGlobalError] = useState('');
  const [success,     setSuccess]     = useState('');
  const [isLoading,   setIsLoading]   = useState(false);
  const [showPwd,     setShowPwd]     = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  // Calculer la force du mot de passe courant
  const strength = passwordStrength(values.password);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setValues(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: undefined }));
    setGlobalError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const fieldErrors = validate(values);
    if (Object.keys(fieldErrors).length > 0) {
      setErrors(fieldErrors);
      return;
    }

    setIsLoading(true);
    setGlobalError('');
    try {
      const res = await registerUser(values);

      if (res.success) {
        setSuccess(res.message || 'Compte créé avec succès ! Redirection en cours…');
        setTimeout(() => router.push(APP_ROUTES.LOGIN), 2500);
      } else {
        setGlobalError(res.message || "Échec de l'inscription.");
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
      title="Créer un compte"
      subtitle="Renseignez vos informations pour accéder à la plateforme GBE"
    >
      {/* Succès */}
      {success && (
        <div className="alert alert--success" role="status">
          <IconCheck /> {success}
        </div>
      )}

      {/* Erreur globale */}
      {globalError && (
        <div className="alert alert--error" role="alert">
          <IconAlert /> {globalError}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>

          {/* Prénom / Nom */}
          <div className="form-grid-2">
            <Input id="firstName" name="firstName" type="text"
              label="Prénom" placeholder="Ali"
              value={values.firstName} onChange={handleChange}
              error={errors.firstName} icon={<IconUser />}
              autoComplete="given-name" disabled={isLoading} />

            <Input id="lastName" name="lastName" type="text"
              label="Nom" placeholder="Bello"
              value={values.lastName} onChange={handleChange}
              error={errors.lastName} icon={<IconUser />}
              autoComplete="family-name" disabled={isLoading} />
          </div>

          {/* Email */}
          <Input id="email" name="email" type="email"
            label="Adresse email" placeholder="vous@exemple.cm"
            value={values.email} onChange={handleChange}
            error={errors.email} icon={<IconMail />}
            autoComplete="email" disabled={isLoading} />

          {/* Matricule / Téléphone */}
          <div className="form-grid-2">
            <Input id="matricule" name="matricule" type="text"
              label="Matricule" placeholder="123456A"
              value={values.matricule} onChange={handleChange}
              error={errors.matricule} icon={<IconId />}
              disabled={isLoading} />

            <Input id="phoneNumber" name="phoneNumber" type="tel"
              label="Téléphone" placeholder="+237 6XX XXX XXX"
              value={values.phoneNumber} onChange={handleChange}
              error={errors.phoneNumber} icon={<IconPhone />}
              autoComplete="tel" disabled={isLoading} />
          </div>

          {/* Date de naissance */}
          <Input id="dateOfBirth" name="dateOfBirth" type="date"
            label="Date de naissance"
            value={values.dateOfBirth} onChange={handleChange}
            error={errors.dateOfBirth} icon={<IconCalendar />}
            autoComplete="bday" disabled={isLoading} />

          {/* Mot de passe + indicateur de force */}
          <div>
            <Input id="password" name="password"
              type={showPwd ? 'text' : 'password'}
              label="Mot de passe" placeholder="••••••••"
              value={values.password} onChange={handleChange}
              error={errors.password} icon={<IconLock />}
              autoComplete="new-password" disabled={isLoading}
              rightElement={
                <button type="button" className="pwd-toggle"
                  onClick={() => setShowPwd(v => !v)}
                  aria-label={showPwd ? 'Masquer' : 'Afficher'}>
                  {showPwd ? <IconEyeOff /> : <IconEye />}
                </button>
              }
            />
            {/* Barres de force */}
            {values.password && (
              <div style={{ marginTop: '6px' }}>
                <div className="pwd-strength">
                  {[1,2,3].map(i => (
                    <div key={i}
                      className={`pwd-strength__bar ${strength >= i ? `pwd-strength__bar--${STRENGTH_MODS[strength]}` : ''}`}
                    />
                  ))}
                </div>
                <p className="pwd-strength__label">
                  Force : {' '}
                  <strong style={{ color: STRENGTH_COLORS[strength] }}>
                    {STRENGTH_LABELS[strength]}
                  </strong>
                </p>
              </div>
            )}
          </div>

          {/* Confirmation mot de passe */}
          <Input id="confirmPassword" name="confirmPassword"
            type={showConfirm ? 'text' : 'password'}
            label="Confirmer le mot de passe" placeholder="••••••••"
            value={values.confirmPassword} onChange={handleChange}
            error={errors.confirmPassword} icon={<IconLock />}
            autoComplete="new-password" disabled={isLoading}
            rightElement={
              <button type="button" className="pwd-toggle"
                onClick={() => setShowConfirm(v => !v)}
                aria-label={showConfirm ? 'Masquer' : 'Afficher'}>
                {showConfirm ? <IconEyeOff /> : <IconEye />}
              </button>
            }
          />

          {/* Bouton inscription */}
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