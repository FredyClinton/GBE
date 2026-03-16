// ============================================================
// FICHIER  : src/app/layout.tsx
// RÔLE     : Layout racine Next.js (App Router).
//            Charge la feuille de style globale et définit
//            les métadonnées HTML de l'application.
// ============================================================

import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title:       "GBE – Gestion du Budget de l'État | MINFI Cameroun",
  description: "Plateforme officielle de gestion du budget de l'État du Cameroun. "
             + "Ministère des Finances (MINFI). Accès réservé aux agents autorisés.",
  keywords:    ['MINFI', 'budget', 'Cameroun', 'GBE', 'finances publiques'],
  icons: { icon: '/images/logo-minfi.png' },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr">
      <body>{children}</body>
    </html>
  );
}