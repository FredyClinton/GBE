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
   → Proposition : `consommerAE`/`libererAE` appelés à la réservation et au rejet, `consommerCP` à l'ordonnancement ; les statuts du crédit ne servent plus qu'à le bloquer (cantonné, suspendu, annulé). Les routes `engager`/`solder` sur un crédit deviennent inutiles.
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
| **1** | Référentiels `Activite` et `Tache` (CRUD admin + consultation, migration V10), fournisseurs (création, RIB), expression de besoin rattachée à une tâche, un chapitre, un exercice et une nature économique (brouillon → publiée → clôturée), consultation des besoins ouverts, soumissions | UC01–UC03 | à faire |
| **2** | Évaluation et attribution, projet d'acte et formes, contrôle CF (réservation des AE, quitus, visa ou rejet), signature | UC04–UC08 | à faire |
| **3** | Stockage de fichiers, livraisons, PV conforme ou non conforme, information de l'ordonnateur | UC09–UC11 | à faire |
| **4** | Demande de paiement, factures et pièces, validation, liquidation, ordonnancement, visa CF, transmission au Trésor, suivi fournisseur | UC12–UC19 | à faire |

### 4.1 Lot 0 — ce qui a été livré

- **Migrations** : V7 (natures → exercice 2026), V8 (rôles, contraintes `CHECK`, permissions des mandats existants), V9 (table `matieres`).
- **Rôles et permissions** : `FOURNISSEUR` (`SOUMISSIONNER`) et `COMPTABLE_MATIERE` (`RECEPTIONNER_LIVRAISON`, `CONSULTER`). Ajouts : `EXPRIMER_BESOIN` et `ORDONNANCER` pour ORDONNATEUR et MINISTRE ; `RESERVER_CREDIT`, `QUITUS_NATURE` et `VISER_ORDONNANCEMENT` pour CONTROLEUR_FINANCIER. Les mandats existants ont reçu ces permissions, le mandat MINISTRE vide a reçu celles du rôle, et le super-admin initial les a toutes.
- **Matières** : simple catalogue (`reference` unique, `libelle`), **sans lien avec la nature économique**. Consultation : `GET /api/v1/referentiel/matieres` et `GET /{id}`. Gestion : `/api/v1/admin/matieres` (`POST`, `PUT /{id}`, `DELETE /{id}`), avec la permission `MANAGE_USERS` comme pour les actions.
- **Crédit** : `consommerAE/CP` refusés sur un crédit cantonné, suspendu, soldé ou annulé (`CREDIT_NON_CONSOMMABLE`). `libererAE/CP` refusent un montant supérieur au consommé. Tout montant doit être > 0. `suspendre` est possible depuis `DISPONIBLE`. `engager`/`solder` (entité et routes) sont `@Deprecated` mais fonctionnent encore, et `ENGAGE` reste consommable.

**Reste à traiter :**
- retirer `engager`/`solder` et les statuts `ENGAGE`/`SOLDE` **après accord du front** ;
- lot 1 : le fournisseur n'a pas de mandat, alors que les permissions ne sont chargées que depuis les mandats (`UserServiceImpl.loadUserByUsername`). Il faudra lui attribuer les permissions par défaut de son rôle ;
- nature économique : portée par l'expression de besoin (lot 1), jamais par la matière. Quand les besoins référenceront les matières, la suppression d'une matière utilisée sera refusée par la clé étrangère ;
- faut-il interdire l'annulation d'un crédit dont des AE sont consommées ? Rien ne l'empêche pour l'instant.

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
