# MINFI - Front-end GBE

Plateforme d'authentification Frontend pour le MINFI (Ministère des Finances du Cameroun).

## Structure du Projet

```
front-gbe/
├── public/
│   └── images/
│       ├── minfi-logo.svg        ← Logo du MINFI
│       └── cameroon-flag.svg     ← Drapeau camerounais
│
├── src/
│   ├── app/
│   │   ├── globals.css           ← Design system (variables, composants)
│   │   ├── layout.tsx            ← Root layout Next.js
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
│   │   │   ├── AuthHeader.tsx    ← En-tête institutionnel
│   │   │   └── AuthLayout.tsx    ← Layout partagé
│   │   └── ui/
│   │       ├── Input.tsx         ← Champ de saisie
│   │       └── Button.tsx        ← Bouton (3 variantes)
│   │
│   ├── constants/
│   │   └── auth.ts               ← URLs API et routes
│   │
│   ├── lib/
│   │   └── authService.ts        ← Appels API
│   │
│   └── types/
│       └── auth.ts               ← Interfaces TypeScript
│
├── .env.example                  ← Variables d'environnement
├── .eslintrc.json
├── .gitignore
├── eslint.config.mjs
├── next.config.ts
├── package.json
├── postcss.config.mjs
├── tsconfig.json
└── README.md

```

## Installation

### Prérequis
- Node.js >= 18
- npm ou yarn

### Étapes
```bash
# Installations des dépendances
npm install

# Copier le fichier d'environnement
cp .env.example .env.local

# Éditer .env.local avec vos configurations
nano .env.local

# Démarrer le serveur de développement
npm run dev
```

L'application sera disponible à `http://localhost:3000`

## Scripts disponibles

```bash
# Développement
npm run dev

# Build de production
npm run build

# Démarrage de la version production
npm start

# Lint (ESLint)
npm run lint

# Lint et correction automatique
npm run lint:fix
```

## Architecture

### Pages d'authentification
- **Login** (`/login`) - Connexion utilisateur
- **Register** (`/register`) - Inscription utilisateur
- **2FA** (`/two-factor`) - Vérification code OTP
- **Forgot Password** (`/forgot-password`) - Récupération du mot de passe

### Composants réutilisables
- **AuthHeader** - En-tête avec logo MINFI et drapeau Cameroun
- **AuthLayout** - Conteneur partagé pour les pages d'auth
- **Button** - Bouton avec 3 variantes (primary, secondary, danger)
- **Input** - Champ de saisie avec gestion d'erreurs

### Services API
- `authService.ts` - Centralise tous les appels API d'authentification

### Types TypeScript
- `auth.ts` - Définit les interfaces pour les payloads et réponses API

### Constantes
- Endpoints API centralisés
- Routes internes de l'application

## Variables d'environnement

Copier `.env.example` en `.env.local` et configurer :

```env
NEXT_PUBLIC_API_URL=http://localhost:3000/api
NEXT_PUBLIC_AUTH_ENABLED=true
NEXT_PUBLIC_SESSION_TIMEOUT=3600
NODE_ENV=development
```

## Technologies utilisées

- **Framework** - Next.js 15+ (React 19)
- **Langage** - TypeScript
- **Styling** - Tailwind CSS
- **Linter** - ESLint
- **Formatage** - Prettier (configuration par ESLint)

## Prochaines étapes

1. [ ] Implémenter les formulaires de connexion/inscription
2. [ ] Intégrer le backend API
3. [ ] Ajouter la gestion des erreurs globale
4. [ ] Mettre en place l'authentification avec tokens JWT
5. [ ] Ajouter les redirections post-authentification
6. [ ] Tester sur mobile
7. [ ] Déployer en production

## Support

Pour toute question ou problème, contactez l'équipe de développement.
