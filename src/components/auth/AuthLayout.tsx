// ============================================================
// FICHIER  : src/components/auth/AuthLayout.tsx
// RÔLE     : Layout partagé par toutes les pages d'auth.
//            Structure (colonne unique centrée) :
//            ┌─────────────────────────────────┐
//            │  AuthHeader (logo + textes)     │  ← bleu marine
//            ├─────────────────────────────────┤
//            │  Titre du formulaire            │
//            │  Sous-titre                     │
//            │  {children} (formulaire)        │
//            │  Footer                         │
//            └─────────────────────────────────┘
// ============================================================

import React from 'react';
import Link  from 'next/link';
import AuthHeader from './AuthHeader';

// ── Props du composant ──
interface AuthLayoutProps {
  /** Titre principal du formulaire (ex : "Connexion") */
  title: string;
  /** Description courte sous le titre */
  subtitle: string;
  /** Contenu du formulaire injecté par chaque page */
  children: React.ReactNode;
}

/**
 * Enveloppe de mise en page pour toutes les pages d'authentification.
 *
 * Usage :
 * ```tsx
 * <AuthLayout title="Connexion" subtitle="Accédez à votre espace">
 *   <LoginForm />
 * </AuthLayout>
 * ```
 */
const AuthLayout: React.FC<AuthLayoutProps> = ({ title, subtitle, children }) => {
  return (
    // Arrière-plan de la page entière
    <div className="auth-page">

      {/* ── En-tête institutionnel (logo MINFI + drapeau + textes) ── */}
      <AuthHeader />

      {/* ── Zone du formulaire ── */}
      <main className="auth-main">

        {/* Carte blanche qui contient le formulaire */}
        <div className="auth-card">

          {/* En-tête de la carte : titre + sous-titre */}
          <div className="auth-card__header">
            <h2 className="auth-card__title">{title}</h2>
            <p  className="auth-card__subtitle">{subtitle}</p>
          </div>

          {/* Corps : formulaire injecté par la page parente */}
          <div className="auth-card__body">
            {children}
          </div>

        </div>

        {/* ── Pied de page ── */}
        <footer className="auth-footer">
          <p>© {new Date().getFullYear()} MINFI – République du Cameroun</p>
          <p>
            <Link href="/aide"    className="auth-footer__link">Aide</Link>
            {' · '}
            <Link href="/contact" className="auth-footer__link">Contact</Link>
          </p>
        </footer>

      </main>
    </div>
  );
};

export default AuthLayout;