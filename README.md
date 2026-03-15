# GBE – Gestion du Budget de l'État
## Ministère des Finances du Cameroun (MINFI)

Application **Next.js 14 + TypeScript** pour la gestion du budget de l'État
selon la **Loi N° 2018/012 du 11 Juillet 2018**.

---

## 🚀 Démarrage rapide

```bash
# 1. Entrer dans le dossier
cd front-gbe

# 2. Installer les dépendances
npm install

# 3. Configurer les variables d'environnement
cp .env.example .env.local
# → Modifier NEXT_PUBLIC_API_URL avec votre URL back-end

# 4. Lancer le serveur de développement
npm run dev
# → http://localhost:3000  (redirige vers /login)
```

---

## 📁 Structure du projet

```
front-gbe/
├── public/
│   └── images/
│       ├── minfi-logo.svg        ← Placeholder (remplacer par le PNG officiel)
│       └── cameroon-flag.svg     ← Drapeau SVG (remplacer par PNG si besoin)
│
├── src/
│   ├── app/
│   │   ├── globals.css           ← Design system complet (variables, composants)
│   │   ├── layout.tsx            ← Root layout Next.js (métadonnées, CSS global)
│   │   ├── page.tsx              ← Redirection "/" → "/login"
│   │   ├── login/
│   │   │   └── page.tsx          ← Page de connexion
│   │   ├── register/
│   │   │   └── page.tsx          ← Page d'inscription
│   │   ├── two-factor/
│   │   │   └── page.tsx          ← Page 2FA (code OTP)
│   │   └── forgot-password/
│   │       └── page.tsx          ← Page mot de passe oublié
│   │
│   ├── components/
│   │   ├── auth/
│   │   │   ├── AuthHeader.tsx    ← En-tête institutionnel (logo + drapeau + textes)
│   │   │   └── AuthLayout.tsx    ← Layout partagé : header + carte + footer
│   │   └── ui/
│   │       ├── Input.tsx         ← Champ de saisie réutilisable
│   │       └── Button.tsx        ← Bouton réutilisable (3 variantes)
│   │
│   ├── constants/
│   │   └── auth.ts               ← URLs API et routes app centralisées
│   │
│   ├── lib/
│   │   └── authService.ts        ← Appels API d'authentification
│   │
│   └── types/
│       └── auth.ts               ← Interfaces TypeScript (payloads, réponses)
│
├── .env.example                  ← Modèle de variables d'environnement
├── .eslintrc.json
├── .gitignore
├── next.config.js
├── package.json
├── postcss.config.js
└── tsconfig.json
```

---

## 🖼️ Images à remplacer

| Fichier                        | Description              | Dimensions conseillées |
|--------------------------------|--------------------------|------------------------|
| `public/images/minfi-logo.png` | Logo officiel MINFI      | 200 × 200 px (carré)   |
| `public/images/cameroon-flag.png` | Drapeau PNG officiel  | 300 × 200 px           |

Après remplacement, mettre à jour `src/components/auth/AuthHeader.tsx` :
```tsx
// Ligne logo
src="/images/minfi-logo.png"

// Ligne drapeau
src="/images/cameroon-flag.png"
```

---

## 🔌 Intégration back-end

Modifier uniquement `src/constants/auth.ts` ou la variable `.env.local` :

```env
NEXT_PUBLIC_API_URL=https://api.gbe.minfi.cm/api/v1
```

### Endpoints attendus

| Méthode | Route                   | Usage                         |
|---------|-------------------------|-------------------------------|
| POST    | `/auth/login`           | Connexion                     |
| POST    | `/auth/register`        | Inscription                   |
| POST    | `/auth/2fa/verify`      | Vérification OTP              |
| POST    | `/auth/2fa/resend`      | Renvoi OTP                    |
| POST    | `/auth/forgot-password` | Demande réinitialisation mdp  |
| POST    | `/auth/reset-password`  | Nouveau mot de passe          |
| POST    | `/auth/logout`          | Déconnexion                   |
| POST    | `/auth/refresh`         | Rafraîchissement du token     |

---

## 🎨 Design

| Élément       | Valeur                                 |
|---------------|----------------------------------------|
| Police titres | Playfair Display (serif élégant)       |
| Police corps  | DM Sans (sans-serif moderne)           |
| Bleu marine   | `#0D2B55` (couleur principale MINFI)   |
| Vert drapeau  | `#007A3D`                              |
| Rouge drapeau | `#CE1126`                              |
| Jaune drapeau | `#FCD116`                              |
| Or MINFI      | `#C9A227`                              |

---

## 📋 Scripts disponibles

```bash
npm run dev        # Serveur de développement (hot reload)
npm run build      # Build de production
npm run start      # Démarrer le build de production
npm run lint       # Vérification ESLint
npm run type-check # Vérification TypeScript sans compilation
```