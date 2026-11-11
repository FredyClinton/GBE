# Module Dépense — plan de mise en œuvre

> Document de travail vivant : à mettre à jour à chaque lot livré ou décision prise.
> Sources : `docs/analyse-fonctionnelle/rapport/` (sections 04 à 09) et
> `docs/analyse-fonctionnelle/diagrammes/modele-metier-et-cycle-de-vie.drawio`
> (la copie de `analyse-fonctionnelle/diagrammes/` contient en plus les énumérations de statuts).

Dernière mise à jour : 2026-11-11

---

## 1. Ce que décrivent les documents

Circuit complet de la dépense, du besoin au paiement. Il fait intervenir 5 acteurs et compte 19 cas d'utilisation (UC01 à UC19).

**Acteurs** : Ordonnateur, Fournisseur (externe), Contrôleur financier (CF), Comptable matière, Comptable public.

| Phase | Étapes | Objets métier |
|---|---|---|
| **1 — Engagement** | besoin → soumissions → attribution → projet d'acte → contrôle CF (réservation des crédits, quitus sur les natures, visa) → signature | `ExpressionBesoin`, `LigneBesoin`, `Matiere`, `Fournisseur`, `Soumission`, `LigneSoumission`, `ActeJuridique` (BCA / LC / Marché / Bail), `Visa`, `ReservationCredit` |
| **2 — Exécution** | livraison → PV de réception → demande de paiement avec factures et pièces justificatives → liquidation → ordonnancement → visa CF → Trésor → paiement | `Livraison`, `ProcesVerbalReception`, `DemandePaiement`, `Facture`, `PieceJustificative`, `Ordonnancement`, `CoordonneeBancaire` |

**Énumérations du modèle** :
- `StatutExpression` : BROUILLON, PUBLIEE, EN_EVALUATION, ATTRIBUEE, ANNULEE, INFRUCTUEUSE
- `StatutSoumission` : DEPOSEE, RECEVABLE, EN_EXAMEN, RETENUE, NON_RETENUE, REJETEE
- `StatutActe` : PROJET, SOUMIS_CONTROLE, VISE_CF, SIGNE_ORDONNATEUR, NOTIFIE_PRESTATAIRE, REJETE, ANNULE
- `StatutVisa` : FAVORABLE, DEFAVORABLE_REJET, SUSPENDU_RESERVES
- `TypeActe` : BON_DE_COMMANDE (BCA), LETTRE_COMMANDE (LC), MARCHE_PUBLIC, CONVENTION_BAIL
- `TypeVisa` : ENGAGEMENT_JURIDIQUE, ORDONNANCEMENT_PAIEMENT

**Règles déjà actées** (rapport, section 9) :
- l'expression de besoin ne porte **aucun prix** : seulement la matière, la quantité et les spécifications ;
- soumission **totale obligatoire** : un prix unitaire pour chaque ligne, les offres partielles sont refusées ;
- **attribution globale** à un seul prestataire ;
- forme de l'acte : le montant fixe le seuil minimal ; la nature, la durée et le choix de l'ordonnateur peuvent imposer un acte plus formalisé.

---

## 2. Écarts avec le code actuel

1. **Statut de `CreditBudgetaire`.** Dans le code, une ligne de crédit passe à `ENGAGE` puis `SOLDE`, comme si elle ne servait qu'à une dépense. Dans le modèle, une ligne finance plusieurs dépenses : chaque `ReservationCredit` bloque des AE, la liquidation consomme des CP.
   → **Fait (lot 0)** : `consommerAE` à la réservation, `libererAE` à l'annulation d'un acte visé, `consommerCP` à l'ordonnancement. Les statuts du crédit ne servent plus qu'à le bloquer (cantonné, suspendu, annulé). Les statuts `ENGAGE`/`SOLDE` et les routes `engager`/`solder` ont été retirés (V10).
2. **Acteurs manquants.** `RoleSysteme` n'a ni `FOURNISSEUR` ni `COMPTABLE_MATIERE` (`COMPTABLE` correspond au comptable public). Le fournisseur est **externe** : pas de fiche `Agent`, pas de `Mandat` de section ; il ne voit que ses propres soumissions et demandes.
3. **Référentiels manquants.**
   - `Matiere` : catalogue des matières (référence, libellé). **Décision 2026-11-11 : aucun lien avec `NatureEconomique`**, car une matière peut relever de plusieurs natures selon le contexte. C'est un écart volontaire avec l'onglet 4 du diagramme (`Matiere` → `NatureEconomique`, « classé dans (quitus CF) »). La nature est portée par l'expression de besoin (`ExpressionBesoin` → `NatureEconomique`, « consomme »).
   - `Activite` et `Tache` (onglet 4 du diagramme) : hiérarchie Programme → Action → Activité → Tâche (chaque lien en 1 → 1..*), avec les champs `code`, `sigle` et `libelle`. L'expression de besoin « s'inscrit dans » **une tâche** (0..* → 1). **Décision 2026-11-11** : comme `Action`, l'activité et la tâche portent aussi `exercice_id` (et section/programme), pour garder des filtres cohérents. Ajoutées au lot 1.
4. **Stockage de fichiers inexistant.** Nécessaire pour les factures, les pièces justificatives, le PV, l'acte signé et le BCA imprimé.
5. **Atout déjà en place.** La nature économique est sur `CreditBudgetaire` (migration V5) : le CF réserve sur la ligne dont la nature correspond au classement des matières du besoin.

**État de la base (2026-11-11)** : les natures économiques (1 072) n'étaient rattachées à **aucun exercice**, et l'endpoint `/api/v1/referentiel/exercice/{id}/natures-economiques` renvoyait une liste vide. C'est corrigé par la migration V7 (lot 0).

---

## 3. Architecture proposée

Les conventions du projet s'appliquent (voir `CLAUDE.md`) : `Entity`, `Repository`, `Service` + `impl/`, `Controller`, MapStruct `Mapper`, `dto/`, une migration Flyway par évolution de schéma.

```
app/
├── referentiel/matiere/            Matiere (catalogue : référence, libellé)
├── identite/fournisseur/           Fournisseur, CoordonneeBancaire (compte User externe)
└── depense/
    ├── besoin/                     ExpressionBesoin, LigneBesoin
    ├── soumission/                 Soumission, LigneSoumission
    ├── engagement/                 ActeJuridique, FormeActe (stratégies), ReservationCredit
    ├── controle/                   Visa (type ENGAGEMENT | ORDONNANCEMENT)
    ├── reception/                  Livraison, ProcesVerbalReception
    ├── liquidation/                DemandePaiement, Facture, PieceJustificative
    └── ordonnancement/             Ordonnancement
common/fichier/                     stockage des pièces (disque local en dev)
```

**Principes** :
- **Cycle de vie** : chaque objet porte son propre statut (énumérations ci-dessus) ; les transitions sont dans l'entité via `validerTransition(...)`, comme dans `CreditBudgetaire`. Le cycle de bout en bout (`BROUILLON` → … → `PAYE_SOLDE`, page 2 du diagramme) est une **vue de suivi calculée**, pas un champ en base.
- **Forme de l'acte** (pattern Strategy du « modèle cible ») : un bean Spring par `TypeActe` ; une `ActeJuridiqueFactory` choisit la forme selon le montant, la nature, la durée et le choix de l'ordonnateur. Les **seuils** sont dans une table de paramétrage, pas dans le code.
- **Montants** : `BigDecimal` partout (le diagramme indique `Double`, à ne pas reprendre).
- **Sécurité** : nouvelles valeurs dans `Permission` (`EXPRIMER_BESOIN`, `SOUMISSIONNER`, `RESERVER_CREDIT`, `QUITUS_NATURE`, `RECEPTIONNER_LIVRAISON`, `ORDONNANCER`, `VISER_ORDONNANCEMENT` ; `VISA_CFI` et `LIQUIDER_DEPENSE` existent déjà), contrôlées par `@PreAuthorize`, avec `MandatScopeValidator` sur la section ou le programme du besoin.

---

## 4. Découpage en lots

Chaque lot comprend sa migration, ses tests et ses routes, et peut être livré seul.

| Lot | Contenu | UC | État |
|---|---|---|---|
| **0** | Rôles `FOURNISSEUR` et `COMPTABLE_MATIERE`, nouvelles permissions, référentiel `Matiere`, refonte du statut des crédits (§2.1), rattachement des natures à l'exercice, permissions par défaut du mandat MINISTRE | — | **livré** (2026-11-11, voir §4.1) |
| **1** | (découpage en §4.2) Référentiels `Activite` et `Tache` (CRUD admin + consultation, migration V11), fournisseurs (création, RIB), expression de besoin rattachée à une tâche, un chapitre, un exercice et une nature économique (brouillon → publiée → clôturée), consultation des besoins ouverts, soumissions | UC01–UC03 | en cours (1a livré) |
| **2** | Évaluation et attribution, projet d'acte et formes, contrôle CF (réservation des AE, quitus, visa ou rejet), signature | UC04–UC08 | à faire |
| **3** | Stockage de fichiers, livraisons, PV conforme ou non conforme, information de l'ordonnateur | UC09–UC11 | à faire |
| **4** | Demande de paiement, factures et pièces, validation, liquidation, ordonnancement, visa CF, transmission au Trésor, suivi fournisseur | UC12–UC19 | à faire |

### 4.1 Lot 0 — ce qui a été livré

- **Migrations** : V7 (natures → exercice 2026), V8 (rôles, contraintes `CHECK`, permissions des mandats existants), V9 (table `matieres`), V10 (statuts des crédits).
- **Rôles et permissions** : `FOURNISSEUR` (`SOUMISSIONNER`) et `COMPTABLE_MATIERE` (`RECEPTIONNER_LIVRAISON`, `CONSULTER`). Ajouts : `EXPRIMER_BESOIN` et `ORDONNANCER` pour ORDONNATEUR et MINISTRE ; `RESERVER_CREDIT`, `QUITUS_NATURE` et `VISER_ORDONNANCEMENT` pour CONTROLEUR_FINANCIER. Les mandats existants ont reçu ces permissions, le mandat MINISTRE vide a reçu celles du rôle, et le super-admin initial les a toutes.
- **Matières** : simple catalogue (`reference` unique, `libelle`), **sans lien avec la nature économique**. Consultation : `GET /api/v1/referentiel/matieres` et `GET /{id}`. Gestion : `/api/v1/admin/matieres` (`POST`, `PUT /{id}`, `DELETE /{id}`), avec la permission `MANAGE_USERS` comme pour les actions.
- **Crédit** : `consommerAE/CP` refusés sur un crédit cantonné, suspendu, soldé ou annulé (`CREDIT_NON_CONSOMMABLE`). `libererAE/CP` refusent un montant supérieur au consommé. Tout montant doit être > 0. `suspendre` part de `DISPONIBLE`. L'annulation est refusée tant que des AE ou des CP sont consommés (`CREDIT_ANNULATION_CREDITS_CONSOMMES`).
- **V10** : retrait des statuts `ENGAGE`/`SOLDE` (les crédits concernés redeviennent `DISPONIBLE`), de `engager()`/`solder()` et des routes `PATCH /api/v1/credits/{id}/engager` et `/solder` (accord donné le 2026-11-11). Statuts restants : `DISPONIBLE`, `CANTONNE`, `SUSPENDU`, `ANNULE`.

**Reste à traiter :**
- **front** : retirer les appels à `engager`/`solder` et les statuts `ENGAGE`/`SOLDE` de l'interface ;
- lot 1 : le fournisseur n'a pas de mandat, alors que les permissions ne sont chargées que depuis les mandats (`UserServiceImpl.loadUserByUsername`). Il faudra lui attribuer les permissions par défaut de son rôle ;
- nature économique : portée par l'expression de besoin (lot 1), jamais par la matière. Quand les besoins référenceront les matières, la suppression d'une matière utilisée sera refusée par la clé étrangère ;

### 4.2 Lot 1 — découpage

Chaque étape est commitée séparément, avec sa migration et ses tests.

| Étape | Contenu | Dépend de | État |
|---|---|---|---|
| **1a** | Référentiels `Activite` et `Tache` (V11) | — | **livré** (2026-11-11) |
| **1b** | `Fournisseur` et `CoordonneeBancaire` (V12) : création par l'admin avec un compte `ROLE_FOURNISSEUR`, RIB, permissions par défaut du rôle sans mandat | lot 0 | à faire |
| **1c** | `ExpressionBesoin` et `LigneBesoin` (V13), UC01 : brouillon, lignes (matière, quantité, spécifications), rattachement tâche / chapitre / exercice / nature, publication, annulation, périmètre du mandat | 1a | à faire |
| **1d** | Consultation des besoins ouverts (UC02) : besoins publiés non échus, filtres, détail | 1c | à faire |
| **1e** | `Soumission` et `LigneSoumission` (V14), UC03 : offre totale, choix du RIB, dépôt verrouillé à la date limite, offre non modifiable une fois déposée, récépissé horodaté | 1b, 1d | à faire |

**1a livré :**
- **Tables `activites` et `taches` :** `code` (unique dans le parent), `sigle`, `libelle`, plus le parent (`action_id` ou `activite_id`). Programme, section et exercice sont recopiés du parent par l'API, et jamais saisis.
- **Consultation :**
  - `GET /api/v1/referentiel/actions/{id}/activites`, `/programmes/{id}/activites?exerciceId=`, `/activites/{id}` ;
  - `GET /api/v1/referentiel/activites/{id}/taches`, `/programmes/{id}/taches?exerciceId=`, `/taches/{id}`.
- **Gestion :** `POST`, `PUT /{id}` et `DELETE /{id}` sous `/api/v1/admin/activites` et `/api/v1/admin/taches`, avec la permission `MANAGE_USERS`.
- **Règles :**
  - déplacer une activité vers une autre action met à jour ses tâches ;
  - suppression refusée pour une activité qui contient des tâches (`ACTIVITE_A_DES_TACHES`) et pour une action qui contient des activités (`ACTION_A_DES_ACTIVITES`, contrôle ajouté à `ActionServiceImpl`).

**Questions avant 1c–1e** (hypothèse par défaut entre parenthèses) :

| # | Question | Hypothèse | Décision |
|---|---|---|---|
| Q7 | Validation du besoin avant publication (fiche objet : Brouillon → Validée → Publiée) ? | non : l'onglet 3 montre une publication directe par l'ordonnateur | — |
| Q8 | `montantEstime` sur le besoin ? L'onglet 3 parle d'« Estimation », alors que la règle actée dit « aucun prix ». | — | — |
| Q9 | Consultation des besoins ouverts : publique sans compte (« portail public ») ou fournisseurs connectés ? | fournisseurs connectés | — |
| Q10 | Pièces justificatives de l'offre au lot 1, ou au lot 3 avec le stockage de fichiers ? | lot 3 | — |
| Q11 | Une seule offre par fournisseur et par besoin ? Retrait possible avant la date limite ? | oui / oui | — |
| Q12 | À la date limite : passage automatique à `EN_EVALUATION`, ou action de l'ordonnateur ? | dépôt bloqué automatiquement, passage par l'ordonnateur | — |

### 4.3 Ce que dit le diagramme d'activité (onglet 3)

Lu le 2026-11-11. Il complète le rapport sur plusieurs points :
- **Besoin :** l'ordonnateur l'exprime (« sélection imputation, matière & estimation ») puis le **publie lui-même**. Le comptable matière n'intervient qu'à la réception.
- **Soumission :** « offre financière, prix unitaire **& RIB** ». Le fournisseur indique au dépôt le RIB à payer, choisi parmi ses coordonnées bancaires.
- **Évaluation :** l'ordonnateur retient une offre. Si aucune n'est valide, le besoin est **déclaré sans suite** (`INFRUCTUEUSE`), ce qui clôt le dossier sans engagement.
- **Contrôle CF :** quitus (classement matière / nature) → réservation des AE → décision. En cas de **refus, le dossier revient à la préparation du projet d'acte**, il n'est pas abandonné. Cohérent avec Q1 et Q2 : validation unique, rien de réservé si refus.
- **Non-conformité :** PV de non-conformité, puis **reprise de la livraison** (boucle). Cela répond à une question du lot 3.
- **Liquidation :** faite par **l'ordonnateur** (« vérification pièces & service fait »). Or `LIQUIDER_DEPENSE` est aujourd'hui une permission par défaut du rôle `COMPTABLE`. **Écart à corriger avant le lot 4.**
- **Visa d'ordonnancement :** le CF contrôle la liquidation et les retenues fiscales (IR), puis transmet au Trésor.
- **Comptable public :** prise en charge du mandat (contrôle de régularité), virement sur le RIB, puis le fournisseur constate le règlement.

---

## 5. Questions ouvertes

Elles bloquent les lots 1 et 2. Les hypothèses s'appliquent tant qu'il n'y a pas de réponse ; elles doivent être notées dans le code et faciles à changer.

| # | Question | Hypothèse par défaut | Décision |
|---|---|---|---|
| Q1 | Contrôle CF : réservation, quitus et visa forment-ils **une seule validation** ou **trois étapes** ? | une seule validation | **Décidé (2026-11-11)** : une seule validation, transactionnelle (réservation + quitus + visa ensemble). À isoler dans le service pour pouvoir passer à trois étapes plus tard. |
| Q2 | Rejet d'un acte par le CF : AE réservées **libérées** ou **maintenues** ? | libérées | **Décidé (2026-11-11)** : libérées. Conséquence de Q1 : rien n'est réservé avant le visa favorable, donc un rejet (`DEFAVORABLE_REJET`) ou une suspension (`SUSPENDU_RESERVES`) ne bloque aucune AE. `libererAE` sert à l'annulation d'un acte déjà visé (`VISE_CF` / `SIGNE_ORDONNATEUR` → `ANNULE`). |
| Q3 | Seuils des formes d'acte (BCA / LC / Marché / Bail) ? | table de paramétrage, sans valeurs inventées | **Décidé (2026-11-11)** : l'ordonnateur choisit la forme ; le système propose la forme minimale et refuse une forme inférieure (échelle BCA < LC < Marché ; Bail hors échelle, imposé par la nature). Tant que la table des seuils est vide, le choix est libre. |
| Q4 | Évaluation des offres **dans l'application**, ou seul le résultat d'une commission externe est saisi ? | seul le résultat est saisi | **Décidé (2026-11-11)** : commission externe, seul le résultat (offre retenue, PV de commission) est saisi. |
| Q5 | Fournisseurs : **auto-inscription** ou création par l'admin ? | création par l'admin | **Décidé (2026-11-11)** : création par l'admin. |
| Q6 | Signature : validation tracée (qui, quand) ou téléversement d'un document signé ? | validation tracée | **Décidé (2026-11-11)** : validation tracée (utilisateur, date). |

Autres questions du rapport (section 9), pour les lots 3 et 4 : livraisons partielles et paiements partiels, procédure en cas de non-conformité, liste des pièces justificatives obligatoires, rôle exact du comptable public (prise en charge, rejet, statut de paiement), limite du suivi fournisseur.

Point à part : le mandat MINISTRE (section MINFI) n'a aucune permission en base. **Décidé (2026-11-11)** : lui donner les permissions par défaut du rôle, par migration (lot 0).

---

## 6. Journal

| Date | Événement |
|---|---|
| 2026-11-11 | Rédaction du plan à partir de l'analyse fonctionnelle. Prérequis en place : réorganisation des packages, Flyway (V1–V6), nature économique sur `CreditBudgetaire`, tests unitaires et d'intégration. |
| 2026-11-11 | Réponses aux questions Q1–Q6 et au point MINISTRE. Q2 (pas de réservation avant visa) et Q3 (choix de la forme avec plancher) confirmées ensuite. Lots 1 et 2 débloqués. |
| 2026-11-11 | Lot 0 livré : V7–V9, rôles et permissions, référentiel `Matiere`, règles de consommation des crédits, routes `engager`/`solder` dépréciées. Tests : 52 unitaires crédit, 8 intégration, tous verts. |
| 2026-11-11 | `Matiere` simplifiée (V9 réécrite, jamais appliquée) : référence + libellé, sans nature économique, unité, description ni statut. |
| 2026-11-11 | Lecture de l'onglet 4 du diagramme : `Activite`/`Tache` (Action → Activité → Tâche, `exercice_id` comme `Action`) ajoutées au lot 1 ; le besoin se rattache à une tâche. Pas de lien matière ↔ nature, confirmé. |
| 2026-11-11 | Lot 0 commité (`2d14474`). Puis : annulation d'un crédit bloquée tant que des AE/CP sont consommés ; `engager`/`solder` et les statuts `ENGAGE`/`SOLDE` retirés (V10). |
| 2026-11-11 | Lot 1 découpé en 1a–1e. **1a livré** : activités et tâches (V11). Lecture de l'onglet 3 (§4.3) : publication directe, RIB au dépôt, refus du CF renvoyé à la préparation de l'acte, liquidation par l'ordonnateur (écart avec `LIQUIDER_DEPENSE` sur `COMPTABLE`). Questions Q7–Q12 ouvertes. |
