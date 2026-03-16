'use client';

// ============================================================
// FICHIER  : src/app/register/qrcode/page.tsx
// RÔLE     : Page affichée après l'inscription réussie.
//
// FLUX :
//   1. L'utilisateur arrive ici après POST /api/v1/auth/register
//   2. La page lit le QR code URL depuis sessionStorage
//      (stocké par register/page.tsx après réponse du back-end)
//   3. Affiche le QR code à scanner avec Google Authenticator
//      ou toute autre app TOTP (Authy, Microsoft Authenticator…)
//   4. Une fois le QR scanné, l'utilisateur clique "J'ai scanné"
//      → redirection vers /login pour se connecter
//
// SÉCURITÉ :
//   - sessionStorage est effacé dès la fermeture de l'onglet
//   - Si aucun QR code n'est trouvé → redirection vers /register
// ============================================================

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Image         from 'next/image';
import Link          from 'next/link';
import Button        from '@/components/ui/Button';
import { APP_ROUTES } from '@/constants/auth';

export default function QrCodePage() {
  const router = useRouter();

  // URL du QR code (data:image/png;base64,... ou URL http)
  const [qrCodeUrl, setQrCodeUrl] = useState<string | null>(null);
  // Email de l'utilisateur pour affichage informatif
  const [userEmail, setUserEmail]  = useState<string>('');
  // État du bouton "J'ai scanné le code"
  const [confirmed, setConfirmed]  = useState(false);

  // ── Lecture du QR code depuis sessionStorage ──
  // useEffect s'exécute côté client uniquement (après hydration)
  useEffect(() => {
    const storedQr    = sessionStorage.getItem('gbe_qr_code');
    const storedEmail = sessionStorage.getItem('gbe_email_2fa');

    if (!storedQr) {
      // Aucun QR code trouvé → l'utilisateur a accédé directement à
      // cette URL sans passer par l'inscription → rediriger
      router.replace(APP_ROUTES.REGISTER);
      return;
    }

    setQrCodeUrl(storedQr);
    setUserEmail(storedEmail ?? '');
  }, [router]);

  // ── Confirmation du scan ──
  const handleConfirm = () => {
    setConfirmed(true);

    // Nettoyage du sessionStorage (sécurité : ne pas laisser traîner le QR)
    sessionStorage.removeItem('gbe_qr_code');
    // Garder gbe_email_2fa pour la page two-factor si besoin

    // Rediriger vers la page de connexion après un court délai
    // pour que l'animation de succès soit visible
    setTimeout(() => {
      router.push(APP_ROUTES.LOGIN);
    }, 1500);
  };

  // ── Affichage de chargement pendant useEffect ──
  if (!qrCodeUrl) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'var(--clr-off-white)',
        fontFamily: 'var(--font-body)',
      }}>
        <div style={{ textAlign: 'center', color: 'var(--clr-gray-400)' }}>
          <div style={{
            width: 40, height: 40,
            border: '3px solid var(--clr-gray-200)',
            borderTopColor: 'var(--clr-navy)',
            borderRadius: '50%',
            animation: 'spin .7s linear infinite',
            margin: '0 auto 16px',
          }} />
          Chargement…
        </div>
      </div>
    );
  }

  return (
    // Page complète avec le fond de l'application
    <div style={{
      minHeight: '100vh',
      background: 'var(--clr-off-white)',
      fontFamily: 'var(--font-body)',
      display: 'flex',
      flexDirection: 'column',
    }}>

      {/* ── En-tête institutionnel ── */}
      <header style={{
        background: 'linear-gradient(160deg, var(--clr-navy) 0%, #091e3a 55%, var(--clr-navy-mid) 100%)',
        padding: '24px 32px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '16px',
        boxShadow: '0 4px 24px rgba(13,43,85,.30)',
        position: 'relative',
      }}>
        {/* Bandes tricolores en bas */}
        <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: 6, display: 'grid', gridTemplateColumns: '1fr 1fr 1fr' }}>
          <div style={{ background: 'var(--clr-green)' }} />
          <div style={{ background: 'var(--clr-red)' }} />
          <div style={{ background: 'var(--clr-yellow)' }} />
        </div>
        {/* Logo */}
        <Image src="/images/logo-minfi.png" alt="MINFI" width={48} height={48}
          style={{ borderRadius: '50%', border: '2px solid rgba(255,255,255,.3)' }} />
        <div style={{ textAlign: 'center', color: '#fff' }}>
          <p style={{ fontFamily: 'var(--font-display)', fontSize: '1rem', fontWeight: 600 }}>
            République du Cameroun
          </p>
          <p style={{ fontSize: '.75rem', color: 'var(--clr-yellow)', letterSpacing: '.1em', textTransform: 'uppercase' }}>
            Ministère des Finances – GBE
          </p>
        </div>
        <Image src="/images/cameroon_flag.jpg" alt="Drapeau" width={60} height={40}
          style={{ borderRadius: 4, border: '1px solid rgba(255,255,255,.2)' }} />
      </header>

      {/* ── Contenu principal ── */}
      <main style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '32px 20px',
      }}>
        <div style={{
          width: '100%',
          maxWidth: 480,
          background: 'var(--clr-white)',
          borderRadius: 'var(--radius-xl)',
          boxShadow: 'var(--shadow-card)',
          overflow: 'hidden',
          animation: 'cardReveal .45s cubic-bezier(.22,.68,0,1.2) both',
        }}>

          {/* Bande tricolore en haut de la carte */}
          <div style={{ height: 4, background: 'linear-gradient(90deg, var(--clr-green), var(--clr-yellow), var(--clr-red))' }} />

          <div style={{ padding: '32px' }}>

            {/* Titre */}
            <div style={{ textAlign: 'center', marginBottom: 24 }}>
              {/* Icône bouclier */}
              <div style={{
                width: 64, height: 64,
                background: 'rgba(13,43,85,.06)',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 16px',
                border: '2px solid rgba(13,43,85,.12)',
              }}>
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--clr-navy)" strokeWidth="1.8">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  <polyline points="9,12 11,14 15,10"/>
                </svg>
              </div>
              <h1 style={{
                fontFamily: 'var(--font-display)',
                fontSize: '1.5rem',
                fontWeight: 700,
                color: 'var(--clr-navy)',
                marginBottom: 8,
              }}>
                Configurez votre authentification
              </h1>
              <p style={{ fontSize: '.875rem', color: 'var(--clr-gray-400)', lineHeight: 1.6 }}>
                Scannez ce QR code avec votre application d&apos;authentification<br/>
                (Google Authenticator, Authy, Microsoft Authenticator…)
              </p>
              {userEmail && (
                <p style={{ fontSize: '.8rem', color: 'var(--clr-navy)', marginTop: 8, fontWeight: 500 }}>
                  Compte : {userEmail}
                </p>
              )}
            </div>

            {/* QR Code */}
            <div style={{
              display: 'flex',
              justifyContent: 'center',
              marginBottom: 24,
            }}>
              <div style={{
                padding: 16,
                background: '#fff',
                borderRadius: 12,
                border: '2px solid var(--clr-gray-100)',
                boxShadow: '0 4px 20px rgba(0,0,0,.08)',
              }}>
                {/* Afficher l'image QR code retournée par le back-end */}
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src={qrCodeUrl}
                  alt="QR Code d'authentification à deux facteurs"
                  style={{ width: 200, height: 200, display: 'block' }}
                />
              </div>
            </div>

            {/* Étapes */}
            <div style={{
              background: 'var(--clr-gray-50)',
              borderRadius: 'var(--radius-md)',
              padding: '16px 20px',
              marginBottom: 24,
            }}>
              <p style={{ fontSize: '.8rem', fontWeight: 600, color: 'var(--clr-gray-600)', marginBottom: 10 }}>
                Comment procéder :
              </p>
              {[
                'Installez Google Authenticator sur votre téléphone',
                'Appuyez sur "+" puis "Scanner un QR code"',
                'Scannez le code ci-dessus avec votre téléphone',
                'Un code à 6 chiffres apparaîtra dans l\'application',
              ].map((step, i) => (
                <div key={i} style={{ display: 'flex', gap: 10, marginBottom: 8, alignItems: 'flex-start' }}>
                  <span style={{
                    width: 20, height: 20,
                    background: 'var(--clr-navy)',
                    color: '#fff',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '.65rem',
                    fontWeight: 700,
                    flexShrink: 0,
                    marginTop: 1,
                  }}>{i + 1}</span>
                  <p style={{ fontSize: '.82rem', color: 'var(--clr-gray-600)', lineHeight: 1.5 }}>{step}</p>
                </div>
              ))}
            </div>

            {/* Bouton confirmation */}
            {!confirmed ? (
              <Button onClick={handleConfirm} fullWidth>
                J&apos;ai scanné le code — Continuer
              </Button>
            ) : (
              /* Animation de succès après confirmation */
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 10,
                padding: '14px',
                background: '#F0FDF4',
                border: '1px solid #BBF7D0',
                borderRadius: 'var(--radius-md)',
                color: '#166534',
                fontSize: '.875rem',
                fontWeight: 500,
                animation: 'fadeSlideDown .3s ease',
              }}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <polyline points="20,6 9,17 4,12"/>
                </svg>
                Configuration réussie ! Redirection vers la connexion…
              </div>
            )}

            {/* Lien retour */}
            <p style={{ textAlign: 'center', marginTop: 16, fontSize: '.82rem', color: 'var(--clr-gray-400)' }}>
              <Link href={APP_ROUTES.LOGIN} style={{ color: 'var(--clr-navy)', fontWeight: 500 }}>
                Aller directement à la connexion
              </Link>
            </p>

          </div>
        </div>

        {/* Pied de page */}
        <footer style={{ marginTop: 24, textAlign: 'center', fontSize: '.72rem', color: 'var(--clr-gray-400)' }}>
          © {new Date().getFullYear()} MINFI – République du Cameroun
        </footer>
      </main>
    </div>
  );
}