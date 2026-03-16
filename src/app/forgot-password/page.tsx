'use client';

// ============================================================
// FICHIER  : src/app/forgot-password/page.tsx
// ============================================================

import React, { useState } from 'react';
import Link      from 'next/link';
import AuthLayout from '@/components/auth/AuthLayout';
import Input      from '@/components/ui/Input';
import Button     from '@/components/ui/Button';
import { APP_ROUTES } from '@/constants/auth';

const IconMail = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="2" y="4" width="20" height="16" rx="2"/>
    <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
  </svg>
);
const IconInfo = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="alert__icon">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="16" x2="12" y2="12"/>
    <line x1="12" y1="8" x2="12.01" y2="8"/>
  </svg>
);

export default function ForgotPasswordPage() {
  const [email,      setEmail]      = useState('');
  const [emailError, setEmailError] = useState('');
  const [submitted,  setSubmitted]  = useState(false);
  const [isLoading,  setIsLoading]  = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) { setEmailError("L'adresse email est requise"); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { setEmailError('Adresse email invalide'); return; }

    setIsLoading(true);
    // Simuler l'envoi (endpoint à implémenter côté back-end)
    await new Promise(r => setTimeout(r, 800));
    setIsLoading(false);
    setSubmitted(true);
  };

  return (
    <AuthLayout
      title="Mot de passe oublié"
      subtitle="Saisissez votre email pour recevoir un lien de réinitialisation"
    >
      {submitted ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20, textAlign: 'center' }}>
          <div style={{ width: 72, height: 72, borderRadius: '50%', background: '#F0FDF4', border: '2px solid #BBF7D0', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '8px auto 0' }}>
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--clr-green)" strokeWidth="2">
              <rect x="2" y="4" width="20" height="16" rx="2"/>
              <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
            </svg>
          </div>
          <div>
            <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.15rem', color: 'var(--clr-navy)', marginBottom: 8 }}>Email envoyé !</h3>
            <p style={{ fontSize: '.875rem', color: 'var(--clr-gray-600)', lineHeight: 1.6 }}>
              Si un compte est associé à <strong style={{ color: 'var(--clr-navy)' }}>{email}</strong>, vous recevrez un lien sous peu.
            </p>
          </div>
          <div className="alert alert--info"><IconInfo />Vérifiez vos dossiers spam.</div>
          <Link href={APP_ROUTES.LOGIN}><Button variant="ghost" fullWidth>Retour à la connexion</Button></Link>
        </div>
      ) : (
        <>
          <form onSubmit={handleSubmit} noValidate>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
              <Input id="email" name="email" type="email"
                label="Adresse email" placeholder="vous@exemple.cm"
                value={email}
                onChange={e => { setEmail(e.target.value); setEmailError(''); }}
                error={emailError} icon={<IconMail />}
                autoComplete="email" autoFocus disabled={isLoading} />
              <Button type="submit" isLoading={isLoading} fullWidth>Envoyer le lien</Button>
            </div>
          </form>
          <p className="auth-switch"><Link href={APP_ROUTES.LOGIN}>← Retour à la connexion</Link></p>
        </>
      )}
    </AuthLayout>
  );
}