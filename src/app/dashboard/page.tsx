'use client';

// ============================================================
// FICHIER  : src/app/dashboard/page.tsx
// RÔLE     : Page d'accueil après connexion réussie.
//            Page simulée — sera remplacée par le vrai dashboard
//            GBE lors du développement de la suite de l'application.
//
// SÉCURITÉ (côté client) :
//   - Vérifie la présence du JWT dans localStorage
//   - Si absent → redirection vers /login
//   - Le JWT doit être validé côté back-end pour les vraies routes
// ============================================================

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Image         from 'next/image';
import { getAccessToken, clearTokens } from '@/lib/authService';
import { APP_ROUTES }                  from '@/constants/auth';

// Modules budgétaires disponibles dans l'application GBE
// (à remplacer par les vrais modules lors du développement)
const MODULES = [
  {
    icon: '📊',
    title: 'Budget de l\'État',
    description: 'Consultation et suivi du budget général',
    color: 'var(--clr-navy)',
    bg: 'rgba(13,43,85,.06)',
  },
  {
    icon: '💰',
    title: 'Recettes',
    description: 'Suivi des recettes budgétaires',
    color: 'var(--clr-green)',
    bg: 'rgba(0,122,61,.06)',
  },
  {
    icon: '📋',
    title: 'Dépenses',
    description: 'Gestion des dépenses publiques',
    color: 'var(--clr-red)',
    bg: 'rgba(206,17,38,.06)',
  },
  {
    icon: '📈',
    title: 'Rapports',
    description: 'Génération de rapports financiers',
    color: 'var(--clr-yellow-dark)',
    bg: 'rgba(212,168,0,.06)',
  },
  {
    icon: '🏛️',
    title: 'Programmes',
    description: 'Suivi des programmes budgétaires',
    color: 'var(--clr-navy-mid)',
    bg: 'rgba(26,58,107,.06)',
  },
  {
    icon: '⚙️',
    title: 'Administration',
    description: 'Paramètres et configuration',
    color: 'var(--clr-gray-600)',
    bg: 'rgba(74,85,104,.06)',
  },
];

export default function DashboardPage() {
  const router = useRouter();

  const [isReady, setIsReady] = useState(false);

  // ── Vérification de l'authentification côté client ──
  useEffect(() => {
    const token = getAccessToken();
    if (!token) {
      // Pas de token → l'utilisateur n'est pas connecté → rediriger
      router.replace(APP_ROUTES.LOGIN);
      return;
    }
    setIsReady(true);
  }, [router]);

  // ── Déconnexion ──
  const handleLogout = () => {
    clearTokens(); // Supprime JWT de localStorage + sessionStorage
    router.push(APP_ROUTES.LOGIN);
  };

  // Affichage pendant la vérification du token
  if (!isReady) {
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
          Chargement de votre espace…
        </div>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: '100vh',
      background: 'var(--clr-off-white)',
      fontFamily: 'var(--font-body)',
      color: 'var(--clr-gray-800)',
    }}>

      {/* ══════════════════════════════════════
          BARRE DE NAVIGATION
          ══════════════════════════════════════ */}
      <nav style={{
        background: 'linear-gradient(135deg, var(--clr-navy) 0%, var(--clr-navy-mid) 100%)',
        padding: '0 32px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: 64,
        boxShadow: '0 2px 16px rgba(13,43,85,.30)',
        position: 'sticky',
        top: 0,
        zIndex: 100,
      }}>
        {/* Logo + nom */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Image src="/images/logo-minfi.png" alt="MINFI" width={36} height={36}
            style={{ borderRadius: '50%', border: '1.5px solid rgba(255,255,255,.3)' }} />
          <div>
            <p style={{ fontFamily: 'var(--font-display)', fontSize: '.9rem', fontWeight: 700, color: '#fff', lineHeight: 1 }}>
              GBE – MINFI
            </p>
            <p style={{ fontSize: '.65rem', color: 'rgba(255,255,255,.55)', letterSpacing: '.05em' }}>
              Gestion du Budget de l&apos;État
            </p>
          </div>
        </div>

        {/* Barre de navigation centrale (placeholder) */}
        <div style={{ display: 'flex', gap: 8 }}>
          {['Accueil', 'Budget', 'Rapports'].map(item => (
            <button key={item} style={{
              background: item === 'Accueil' ? 'rgba(255,255,255,.12)' : 'transparent',
              border: 'none',
              color: '#fff',
              fontSize: '.82rem',
              fontWeight: 500,
              padding: '6px 14px',
              borderRadius: 6,
              cursor: 'pointer',
              fontFamily: 'var(--font-body)',
            }}>
              {item}
            </button>
          ))}
        </div>

        {/* Bouton déconnexion */}
        <button
          onClick={handleLogout}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            background: 'rgba(255,255,255,.1)',
            border: '1px solid rgba(255,255,255,.2)',
            color: '#fff',
            fontSize: '.82rem',
            fontWeight: 500,
            padding: '7px 14px',
            borderRadius: 8,
            cursor: 'pointer',
            fontFamily: 'var(--font-body)',
            transition: 'background .15s',
          }}
          onMouseEnter={e => (e.currentTarget.style.background = 'rgba(255,255,255,.18)')}
          onMouseLeave={e => (e.currentTarget.style.background = 'rgba(255,255,255,.10)')}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16,17 21,12 16,7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          Déconnexion
        </button>
      </nav>

      {/* ══════════════════════════════════════
          CONTENU PRINCIPAL
          ══════════════════════════════════════ */}
      <main style={{ maxWidth: 1100, margin: '0 auto', padding: '40px 24px' }}>

        {/* Bannière de bienvenue */}
        <div style={{
          background: 'linear-gradient(135deg, var(--clr-navy) 0%, var(--clr-navy-mid) 100%)',
          borderRadius: 'var(--radius-xl)',
          padding: '32px 40px',
          marginBottom: 40,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 8px 32px rgba(13,43,85,.20)',
          position: 'relative',
          overflow: 'hidden',
        }}>
          {/* Motif décoratif */}
          <div style={{
            position: 'absolute', right: -40, top: -40,
            width: 200, height: 200,
            borderRadius: '50%',
            background: 'rgba(255,255,255,.04)',
            pointerEvents: 'none',
          }} />
          <div style={{
            position: 'absolute', right: 60, bottom: -60,
            width: 160, height: 160,
            borderRadius: '50%',
            background: 'rgba(252,209,22,.06)',
            pointerEvents: 'none',
          }} />
          {/* Bandes tricolores en bas */}
          <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: 4, display: 'grid', gridTemplateColumns: '1fr 1fr 1fr' }}>
            <div style={{ background: 'var(--clr-green)' }} />
            <div style={{ background: 'var(--clr-red)' }} />
            <div style={{ background: 'var(--clr-yellow)' }} />
          </div>

          <div style={{ position: 'relative', zIndex: 1 }}>
            <p style={{ color: 'var(--clr-yellow)', fontSize: '.75rem', fontWeight: 500, letterSpacing: '.15em', textTransform: 'uppercase', marginBottom: 8 }}>
              Bienvenue sur la plateforme
            </p>
            <h1 style={{
              fontFamily: 'var(--font-display)',
              fontSize: '1.8rem',
              fontWeight: 700,
              color: '#fff',
              marginBottom: 8,
            }}>
              Gestion du Budget de l&apos;État
            </h1>
            <p style={{ color: 'rgba(255,255,255,.65)', fontSize: '.875rem' }}>
              Ministère des Finances du Cameroun • Exercice budgétaire {new Date().getFullYear()}
            </p>
          </div>

          <Image src="/images/logo-minfi.png" alt="MINFI" width={80} height={80}
            style={{
              borderRadius: '50%',
              border: '2px solid rgba(255,255,255,.2)',
              position: 'relative',
              zIndex: 1,
              opacity: .9,
            }} />
        </div>

        {/* Indicateur page en développement */}
        <div style={{
          background: '#FFF7ED',
          border: '1px solid #FED7AA',
          borderRadius: 'var(--radius-md)',
          padding: '12px 16px',
          marginBottom: 32,
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          fontSize: '.82rem',
          color: '#9A3412',
        }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ flexShrink: 0 }}>
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          <span>
            <strong>Tableau de bord en développement</strong> — Vous êtes connecté avec succès.
            Les modules ci-dessous seront disponibles dans les prochaines versions.
          </span>
        </div>

        {/* Grille des modules */}
        <h2 style={{
          fontFamily: 'var(--font-display)',
          fontSize: '1.2rem',
          fontWeight: 700,
          color: 'var(--clr-navy)',
          marginBottom: 20,
        }}>
          Modules disponibles
        </h2>

        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
          gap: 20,
        }}>
          {MODULES.map((mod, i) => (
            <div key={i} style={{
              background: '#fff',
              borderRadius: 'var(--radius-lg)',
              padding: '24px',
              boxShadow: 'var(--shadow-sm)',
              border: '1px solid var(--clr-gray-100)',
              display: 'flex',
              alignItems: 'flex-start',
              gap: 16,
              cursor: 'default',
              transition: 'transform .2s, box-shadow .2s',
              animation: `cardReveal .4s ${i * .06}s cubic-bezier(.22,.68,0,1.2) both`,
            }}
            onMouseEnter={e => {
              e.currentTarget.style.transform = 'translateY(-3px)';
              e.currentTarget.style.boxShadow = 'var(--shadow-md)';
            }}
            onMouseLeave={e => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = 'var(--shadow-sm)';
            }}
            >
              {/* Icône du module */}
              <div style={{
                width: 48, height: 48,
                background: mod.bg,
                borderRadius: 12,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.4rem',
                flexShrink: 0,
              }}>
                {mod.icon}
              </div>
              <div>
                <h3 style={{ fontSize: '.9rem', fontWeight: 600, color: mod.color, marginBottom: 4 }}>
                  {mod.title}
                </h3>
                <p style={{ fontSize: '.8rem', color: 'var(--clr-gray-400)', lineHeight: 1.5 }}>
                  {mod.description}
                </p>
              </div>
            </div>
          ))}
        </div>

        {/* Loi de référence */}
        <div style={{
          marginTop: 40,
          padding: '16px 20px',
          background: 'rgba(13,43,85,.04)',
          borderRadius: 'var(--radius-md)',
          border: '1px solid rgba(13,43,85,.08)',
          fontSize: '.75rem',
          color: 'var(--clr-gray-400)',
          textAlign: 'center',
          fontStyle: 'italic',
        }}>
          Application développée conformément à la Loi N° 2018/012 du 11 Juillet 2018
          portant régime financier de l&apos;État et des autres entités publiques
          – République du Cameroun
        </div>

      </main>
    </div>
  );
}