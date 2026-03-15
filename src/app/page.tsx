// ============================================================
// FICHIER  : src/app/page.tsx
// RÔLE     : Redirection automatique de la racine "/" vers
//            la page de connexion "/login".
// ============================================================

import { redirect } from 'next/navigation';

export default function HomePage() {
  redirect('/login');
}