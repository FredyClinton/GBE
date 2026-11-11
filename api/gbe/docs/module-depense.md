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
| **1** | (découpage en §4.2) Référentiels `Activite` et `Tache` (CRUD admin + consultation, migration V11), fournisseurs (création, RIB), expression de besoin rattachée à une tâche, un chapitre, un exercice et une nature économique (brouillon → publiée → clôturée), consultation des besoins ouverts, soumissions | UC01–UC03 | **livré** (2026-11-11) |
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
| **1b** | `Fournisseur` et `CoordonneeBancaire` (V12–V14) : création par l'admin avec un compte `ROLE_FOURNISSEUR`, RIB, permissions par défaut du rôle sans mandat | lot 0 | **livré** (2026-11-11) |
| **1c** | `ExpressionBesoin` et `LigneBesoin` (V15), UC01 : brouillon, lignes (matière, quantité, spécifications), rattachement tâche / chapitre / exercice / nature, publication, annulation, périmètre du mandat | 1a | **livré** (2026-11-11) |
| **1d** | Consultation des besoins ouverts (UC02) : besoins publiés non échus, filtres, détail | 1c | **livré** (2026-11-11) |
| **1e** | `Soumission` et `LigneSoumission` (V16), UC03 : offre totale, choix du RIB, dépôt verrouillé à la date limite, offre modifiable jusqu'à la date limite, récépissé horodaté et scellé | 1b, 1d | **livré** (2026-11-11) |

**1a livré :**
- **Tables `activites` et `taches` :** `code` (unique dans le parent), `sigle`, `libelle`, plus le parent (`action_id` ou `activite_id`). Programme, section et exercice sont recopiés du parent par l'API, et jamais saisis.
- **Consultation :**
  - `GET /api/v1/referentiel/actions/{id}/activites`, `/programmes/{id}/activites?exerciceId=`, `/activites/{id}` ;
  - `GET /api/v1/referentiel/activites/{id}/taches`, `/programmes/{id}/taches?exerciceId=`, `/taches/{id}`.
- **Gestion :** `POST`, `PUT /{id}` et `DELETE /{id}` sous `/api/v1/admin/activites` et `/api/v1/admin/taches`, avec la permission `MANAGE_USERS`.
- **Règles :**
  - déplacer une activité vers une autre action met à jour ses tâches ;
  - suppression refusée pour une activité qui contient des tâches (`ACTIVITE_A_DES_TACHES`) et pour une action qui contient des activités (`ACTION_A_DES_ACTIVITES`, contrôle ajouté à `ActionServiceImpl`).

**1b livré :**
- **Fournisseur :** `raisonSociale`, `niu` (unique), `rccm`, `adresse`, `telephone` (de l'entreprise, ajouté en V14 comme dans le diagramme de classes). Il est lié à un compte `User` créé en même temps : rôle `FOURNISSEUR`, nom, prénom et téléphone du représentant (`telephoneRepresentant`, unique), email, mot de passe provisoire, `firstLogin`. Pas d'agent, pas de mandat.
- **Coordonnées bancaires :** **seul le RIB** est stocké (décision 2026-11-11) : 23 caractères, banque 5 chiffres, guichet 5 chiffres, compte 11 caractères, clé 2 chiffres. Les espaces et tirets sont retirés, et un fournisseur peut avoir plusieurs RIB (choix au dépôt, onglet 3). La clé RIB n'est pas recalculée.
- **Routes admin** (`MANAGE_USERS`) : `POST`/`GET`/`GET {id}`/`PUT {id}` sous `/api/v1/admin/fournisseurs`, `POST {id}/ribs`, `DELETE {id}/ribs/{ribId}`. Activation du compte : routes `/api/v1/admin/users/{userId}/activate|deactivate` existantes. La suppression d'un compte fournisseur est refusée (`FOURNISSEUR_LIE_AU_COMPTE`).
- **Espace fournisseur :** `GET /api/v1/fournisseurs/moi` (`ROLE_FOURNISSEUR`).
- **Sécurité :** `RoleSysteme.isExterne()`. Un rôle externe ne peut pas porter de mandat (`ROLE_EXTERNE_SANS_MANDAT`), y compris via `/admin/users`, et reçoit directement les permissions par défaut de son rôle.
- **Point 5 (V12) :** `LIQUIDER_DEPENSE` passe de COMPTABLE à ORDONNATEUR et MINISTRE (décision 2026-11-11). L'écart du §4.3 est corrigé.
- **Bug corrigé :** un refus de `@PreAuthorize` renvoyait **500** sur toutes les routes, au lieu de 403. Il renvoie maintenant **403 `ACCES_REFUSE_PERMISSION`**. **À signaler au front.**
- **Reste :** le fournisseur ne gère pas lui-même ses RIB (l'admin le fait). Retirer un RIB déjà utilisé par une soumission devra être refusé (1e).

**1c livré :**
- **`ExpressionBesoin` :** `reference` (`EB-<année>-00001`, générée par exercice), `objet`, `dateExpression`, `dateClotureSoumission` (date **et heure**), `datePublication`, `dateClotureEffective`, `statut`, `motifAnnulation`. Elle est rattachée à une **tâche** (exercice, section et programme recopiés), à un **chapitre** de la même section (structure émettrice) et à une **nature économique** de l'exercice. Pas de montant (Q8), pas de conditions de recevabilité (décision 2026-11-11).
- **`LigneBesoin` :** `numeroOrdre` (automatique), matière, `quantiteDemandee` > 0, `specifications`. C'est un champ libre, qui porte aussi l'unité. **Une matière ne figure qu'une fois par besoin.**
- **Cycle de vie (dans l'entité) :**
  - `BROUILLON` → `PUBLIEE` : au moins une ligne et une date limite future ;
  - `PUBLIEE` → `EN_EVALUATION` : action de l'ordonnateur, **après** la date limite ;
  - `PUBLIEE` → `ANNULEE` : motif obligatoire ;
  - un brouillon est **supprimé logiquement** (`supprime`, exclu des requêtes).
- **Modification :** possible en brouillon, et une fois publié **tant qu'aucune soumission n'est déposée** et que la date limite n'est pas passée. Un besoin publié doit rester complet. Le test « aucune soumission » renvoie toujours faux pour l'instant : **à brancher en 1e**.
- **Routes `/api/v1/besoins` :**
  - `POST`, `PUT /{id}`, `PATCH /{id}/publier|cloturer|annuler` et `DELETE /{id}` demandent `EXPRIMER_BESOIN` sur le périmètre ;
  - `GET` (filtres `exerciceId`, `sectionId`, `programmeId`, `statut`) et `GET /{id}` sont limités au périmètre des mandats.
- **Technique :**
  - `MandatScopeValidator.checkAccessPerimetre` et `perimetreCourant` (contrôle générique, hors crédit), avec la nouvelle erreur `ACCES_REFUSE_HORS_PERIMETRE` ;
  - `ReferenceGenerator` (table `compteurs_reference`) ;
  - bean `Clock` pour les dates métier.

**1d livré :**
- **Consultation publique, sans compte** (Q9) : `GET /api/v1/public/besoins` et `GET /api/v1/public/besoins/{id}`, en **GET uniquement** (`SecurityConfig.PUBLIC_GET_URLS`). Il faudra être connecté pour soumissionner (1e).
- **Contenu visible :** seulement les besoins **publiés dont la date limite n'est pas atteinte**. Un brouillon, un besoin échu ou clos répond 404, comme un identifiant inconnu. La réponse ne contient aucun identifiant interne de tâche, de programme ou d'exercice. Elle donne la référence, l'objet, les dates, la section, la structure émettrice (libellé du chapitre) et la nature économique, plus les lignes dans le détail.
- **Filtres (UC02) :**
  - `motCle`, cherché dans l'objet et dans la référence ou le libellé des matières ;
  - `natureEconomiqueId` (famille de prestations) ;
  - `dateLimiteAvant` (échéance au plus tard).
  Tri par défaut : échéance la plus proche d'abord.
- **Pagination**, sur cette liste et sur `GET /api/v1/besoins` (décision 2026-11-11) :
  - format `PageResponse` : `content`, `page`, `size`, `totalElements`, `totalPages` ;
  - `size` de 20 par défaut, 100 au maximum ;
  - tri limité à une liste de propriétés (`TRI_INVALIDE` sinon) ;
  - le périmètre des mandats est appliqué **dans la requête** (`MandatScopeValidator.Perimetre`), et non plus en mémoire.
- **Changement de format :** `GET /api/v1/besoins` (livré en 1c) renvoie maintenant une page au lieu d'un tableau.

**1e livré** (d'après les classes `Soumission` et `LigneSoumission` de l'onglet 4, l'onglet 3 et la fiche UC03) :
- **`Soumission` :** `numero` (`SO-<année>-00001`), `dateSoumission` (horodatage du dépôt), `dateDerniereModification`, `montantProposeHT` (**calculé** : somme des lignes), `montantProposeTTC`, `delaiProposeJours` (un seul délai, sur l'offre, comme dans l'onglet 4), `statut` (`StatutSoumission`, seul `DEPOSEE` est utilisé au lot 1).
- **`LigneSoumission` :** `numeroOrdre` (celui de la ligne de besoin), `prixUnitairePropose` (> 0), `montantTotalLigneHT` (= prix × quantité, `calculerSousTotalHT`), `remarquesTechniques`.
- **Règles :**
  - **offre totale**, contrôlée par `definirOffre` (`validerCompletude` du modèle) : un prix pour chaque ligne du besoin, ni plus ni moins ;
  - **une soumission par fournisseur et par besoin** (409 `SOUMISSION_DEJA_DEPOSEE`) ;
  - dépôt seulement sur un besoin ouvert (`BESOIN_NON_OUVERT`) ;
  - **modifiable jusqu'à la date limite** (Q11) ;
  - **pas de retrait** et **pas de pièce jointe** (décisions 2026-11-11, conformes au diagramme de classes).
- **TTC :** calculé via l'interface **`CalculTaxe`** (§4.4). Méthode provisoire `TtcSaisiParFournisseur` : TTC saisi, au moins égal au HT.
- **Compléments à la classe `Soumission`, issus de la conception :**
  - **lien vers la `CoordonneeBancaire` choisie au dépôt** parmi celles du fournisseur (onglet 3 : « offre financière, prix unitaire & RIB »). Le RIB reste un attribut de `CoordonneeBancaire` (onglet 4 : `rib`) : la soumission ne le recopie pas, elle référence la coordonnée (`coordonnee_bancaire_id`). Seule l'association `Soumission → CoordonneeBancaire` est ajoutée : l'onglet 4 ne relie `CoordonneeBancaire` qu'au `Fournisseur` (« possède ») ;
  - **empreinte** SHA-256 recalculée à chaque modification (fiche UC03 : « récépissé scellé et horodaté »).
- **Écart volontaire :** UC03 dit que l'offre est « enregistrée de façon inaltérable ». La décision Q11 la rend modifiable jusqu'à la date limite. Chaque version est horodatée et scellée.
- **Contrôles branchés ailleurs :**
  - un besoin publié n'est plus modifiable dès qu'une soumission existe ;
  - un RIB utilisé par une soumission ne peut plus être retiré (`RIB_UTILISE`).
- **Routes `/api/v1/soumissions`** (permission `SOUMISSIONNER`, donc fournisseur connecté) : `POST` (récépissé), `PUT /{id}`, `GET` (paginé, les siennes), `GET /{id}`. La soumission d'un autre fournisseur répond 404.
- **Pour le lot 2 :**
  - consultation des offres par l'ordonnateur : faut-il interdire de les voir avant la clôture ? La conception ne le dit pas ;
  - sort des soumissions quand un besoin publié est annulé.

**Questions avant 1c–1e** (hypothèse par défaut entre parenthèses) :

| # | Question | Hypothèse | Décision |
|---|---|---|---|
| Q7 | Validation du besoin avant publication (fiche objet : Brouillon → Validée → Publiée) ? | non : l'onglet 3 montre une publication directe par l'ordonnateur | **Décidé (2026-11-11)** : publication directe. |
| Q8 | `montantEstime` sur le besoin ? L'onglet 3 parle d'« Estimation », alors que la règle actée dit « aucun prix ». | — | **Décidé (2026-11-11)** : pas de montant estimé. |
| Q9 | Consultation des besoins ouverts : publique sans compte (« portail public ») ou fournisseurs connectés ? | fournisseurs connectés | **Décidé (2026-11-11)** : consultation publique sans compte ; connexion obligatoire pour soumissionner. |
| Q10 | Pièces justificatives de l'offre au lot 1, ou au lot 3 avec le stockage de fichiers ? | pas de pièce jointe en 1e | **Décidé (2026-11-11)** : pas de pièce jointe, comme dans le diagramme de classes. **Écart constaté** : la fiche UC03 parle de « justificatifs légaux, administratifs et techniques », mais le diagramme de classes ne rattache aucun document à `Soumission` (les pièces ne concernent que la demande de paiement, UC13). À confirmer, et à clarifier avec l'analyse fonctionnelle : pièces par fournisseur ou par offre. |
| Q11 | Une seule offre par fournisseur et par besoin ? Retrait possible avant la date limite ? | oui / oui | **Décidé (2026-11-11)** : le fournisseur peut **modifier** son offre jusqu'à la clôture des soumissions (date limite) ; une offre par fournisseur et par besoin. Pas de retrait (absent de `StatutSoumission`), décidé le 2026-11-11. |
| Q12 | À la date limite : passage automatique à `EN_EVALUATION`, ou action de l'ordonnateur ? | dépôt bloqué automatiquement, passage par l'ordonnateur | **Décidé (2026-11-11)** : comme l'hypothèse. |

### 4.3 Ce que dit le diagramme d'activité (onglet 3)

Lu le 2026-11-11. Il complète le rapport sur plusieurs points :
- **Besoin :** l'ordonnateur l'exprime (« sélection imputation, matière & estimation ») puis le **publie lui-même**. Le comptable matière n'intervient qu'à la réception.
- **Soumission :** « offre financière, prix unitaire **& RIB** ». Le fournisseur indique au dépôt le RIB à payer, choisi parmi ses coordonnées bancaires.
- **Évaluation :** l'ordonnateur retient une offre. Si aucune n'est valide, le besoin est **déclaré sans suite** (`INFRUCTUEUSE`), ce qui clôt le dossier sans engagement.
- **Contrôle CF :** quitus (classement matière / nature) → réservation des AE → décision. En cas de **refus, le dossier revient à la préparation du projet d'acte**, il n'est pas abandonné. Cohérent avec Q1 et Q2 : validation unique, rien de réservé si refus.
- **Non-conformité :** PV de non-conformité, puis **reprise de la livraison** (boucle). Cela répond à une question du lot 3.
- **Liquidation :** faite par **l'ordonnateur** (« vérification pièces & service fait »). `LIQUIDER_DEPENSE` était une permission par défaut du rôle `COMPTABLE`. **Corrigé en 1b (V12).**
- **Visa d'ordonnancement :** le CF contrôle la liquidation et les retenues fiscales (IR), puis transmet au Trésor.
- **Comptable public :** prise en charge du mandat (contrôle de régularité), virement sur le RIB, puis le fournisseur constate le règlement.

### 4.4 Évolutions prévues

**Méthodes de calcul des taxes selon le type de besoin** (demandé le 2026-11-11, à faire plus tard)

- **Besoin :** le calcul des taxes (TVA et autres retenues) dépendra du **type de besoin**. Les méthodes de calcul seront introduites plus tard.
- **Ce que dit la conception** (vérifié le 2026-11-11 : drawio, tous onglets, deux copies, et rapport) :
  - **pas de champ « type de besoin »** ;
  - montants : `montantProposeHT` et `montantProposeTTC` sur `Soumission` (onglets 1 et 4), `montantHT`, `montantTVA` et `montantTTC` sur `Facture` (fiche objet) ;
  - UC15 : l'ordonnateur vérifie « les taux de taxes » sur la facture ;
  - UC16 : retenues de garantie et pénalités à la liquidation (`tauxRetenueGarantie` sur `StrategieMarchePublic`) ;
  - onglet 3 : « retenues fiscales (IR) » contrôlées par le CF au visa d'ordonnancement ;
  - aucune règle ne relie un taux à un type de besoin.
- **Ce qui peut servir de « type » dans la conception :**
  - la **nature économique du besoin** (`ExpressionBesoin` → `NatureEconomique`, « consomme », déjà en place en 1c ; UC02 parle de « famille de prestations ») ;
  - à défaut, le **`TypeActe`** de l'acte juridique (lot 2).
- **À définir :**
  - la liste des types de besoin ;
  - pour chaque type, la ou les taxes, leur taux ou leur règle, et les exonérations éventuelles ;
  - les objets concernés : TTC de la soumission, TVA de la facture (lot 4), retenues à l'ordonnancement.
- **Proposition :** **ne pas ajouter de champ** ; s'appuyer d'abord sur la nature économique du besoin. N'introduire un vrai « type de besoin » que si elle ne suffit pas, et **d'abord dans le drawio**. Calcul par **stratégie** (même principe que `FormeActeStrategie`), avec des paramètres en table plutôt que dans le code.
- **Préparation dès 1e :** le calcul du TTC d'une offre passe par une seule interface (`CalculTaxe`), qui reçoit le besoin (donc sa nature), pour pouvoir brancher plus tard une méthode par type de besoin sans toucher au reste du code. En attendant, le comportement provisoire est celui du modèle : le fournisseur saisit le TTC, et le serveur vérifie seulement que TTC ≥ HT.

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
| 2026-11-11 | **1b livré** : fournisseurs et RIB (V13), rôle externe sans mandat, `LIQUIDER_DEPENSE` déplacée vers l'ordonnateur (V12), refus d'autorisation en 403 au lieu de 500. Q8 : pas de montant estimé. |
| 2026-11-11 | V14 : `telephone` de l'entreprise sur `Fournisseur` (déjà présent dans le diagramme de classes). Le téléphone du compte devient `telephoneRepresentant` dans l'API. |
| 2026-11-11 | **1c livré** : expressions de besoin (V15). Décisions : unité dans les spécifications, pas de conditions de recevabilité, référence `EB-AAAA-00001`, date limite avec heure, modification autorisée tant qu'aucune soumission, matière unique par besoin, suppression logique des brouillons, clôture par l'ordonnateur après la date limite (Q7, Q12). |
| 2026-11-11 | **1d livré** : consultation publique des besoins ouverts (Q9 : public, connexion pour soumissionner). Pagination `PageResponse` sur les listes de besoins, périmètre filtré en base. |
| 2026-11-11 | Q11 : offre modifiable jusqu'à la date limite. Q10 : écart entre la fiche UC03 (justificatifs) et le diagramme de classes (aucun document sur `Soumission`). Restent avant 1e : Q10, retrait de l'offre, calcul HT/TTC (TVA), délai unique sur l'offre. |
| 2026-11-11 | Évolution notée (§4.4) : méthodes de calcul des taxes selon le type de besoin, à introduire plus tard. Le TTC de la soumission passera par une interface `CalculTaxe` dès 1e (provisoire : TTC saisi par le fournisseur, TTC ≥ HT). |
| 2026-11-11 | §4.4 corrigé : la conception n'a pas de « type de besoin ». Le calcul des taxes s'appuiera d'abord sur la nature économique du besoin (ou le `TypeActe`). Pas de nouveau champ sans passer d'abord par le drawio. |
| 2026-11-11 | **1e livré, lot 1 terminé** : soumissions (V16), offre totale, HT calculé, TTC via `CalculTaxe` (provisoire : saisi, ≥ HT), une offre par fournisseur et par besoin, modifiable jusqu'à la date limite, RIB choisi, récépissé scellé. Q10 : pas de pièce jointe. Pas de retrait. Délai unique sur l'offre. |
| 2026-11-11 | Précision : le RIB est un attribut de `CoordonneeBancaire` ; la soumission référence la coordonnée choisie (association absente de l'onglet 4, déduite de l'onglet 3). |
| 2026-11-11 | Confirmé : un fournisseur peut avoir plusieurs RIB et choisit librement celui de chaque offre (au dépôt et à chaque modification). Rien à changer au code. |
| 2026-11-11 | Documentation Swagger de tous les contrôleurs (`@Operation`, `@ApiResponses`, `@Parameter`) et regroupement des tags : Authentification, Mon compte, Administration · …, Référentiel · … (consultation et administration ensemble), Budget, Dépense · …, Espace fournisseur. Test de non-régression sur `/v3/api-docs`. |
