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
- **Préparation dès 1e :** le calcul du TTC d'une offre passe par une seule interface (`CalculTaxe`), qui reçoit le besoin (donc sa nature), pour pouvoir brancher plus tard une méthode par type de besoin sans toucher au reste du code.
- **Les taxes dépendent du type d'acte juridique** (précisé par l'utilisateur, 2026-11-11). **Seules les règles du BCA sont connues** (TVA 19,25 %, IR 5,5 %) : on fait avec. Le code actuel est donc juste pour le BCA, seule forme traitée. La conception ne relie aucun taux au type d'acte ; seule `StrategieMarchePublic` porte des retenues propres (`tauxRetenueGarantie`, `penaliteJour`).
- **Depuis 2b (V18, Q23) :** `TvaEtIrParametres` remplace le TTC saisi par le fournisseur. TVA et IR sont appliqués au HT avec les taux de la table `taxes` ; TTC = HT + TVA ; NAP = TTC − TVA − IR. Les méthodes par type de besoin et par régime fiscal restent à faire (§7).

### 4.5 Lot 2 — découpage (2026-11-11, Q13–Q22 décidées)

Sources relues : fiches UC04–UC08 et fiches objet (`ActeJuridique`, `Credit`, `QuitusNatureEconomique`, `Visa`) du
rapport, section 9 ; drawio, onglets 1, 2, 3 et 4 (deux copies).

| Étape | Contenu | Source | Dépend de |
|---|---|---|---|
| **2a** (**livré**) | **Attribution** : saisie du résultat de la commission (Q4), `EN_EVALUATION → ATTRIBUEE` (`retenirSoumission`) ou `→ INFRUCTUEUSE` (`infructueux()`, « déclarer sans suite ») ; offre retenue `RETENUE`, les autres `NON_RETENUE` ; consultation des offres par l'ordonnateur | onglet 2, onglet 3 (« Évaluer les offres », « Offre valide ? »), Q4 | 1e |
| **2b** (**livré**) | **Projet d'acte** (UC04) : `ActeJuridique` créé par `ActeJuridiqueFactory` à partir du besoin et de l'offre retenue ; `TypeActe` et une `FormeActeStrategie` par type (BCA, LC, marché, bail) ; forme minimale proposée, forme inférieure refusée (Q3) ; table des seuils (vide → choix libre) ; `PROJET` modifiable → `SOUMIS_CONTROLE` (plus modifiable) | UC04, onglet 4 (factory, stratégies), onglet 1 (`type : TypeActe`), §9 « forme de l'acte » | 2a |
| **2c** (**livré**) | **Contrôle CF** (UC06 « include » UC07, UC08) : une seule décision transactionnelle (Q1) ; `Visa` (`ENGAGEMENT_JURIDIQUE`) ; si favorable : quitus + `ReservationCredit` + `consommerAE` sur la ligne + acte `VISE_CF` ; sinon rien n'est réservé (Q2) et le dossier revient à l'ordonnateur | UC06–UC08, onglet 2 (`viser`, `rejeter(motif)`), onglet 3 (« refus / rejet » → préparer le projet d'acte), onglet 4 (`Visa`, `ReservationCredit`) | 2b |
| **2d** | **Signature et notification** (UC05) : `VISE_CF → SIGNE_ORDONNATEUR` (validation tracée : signataire, date, Q6) puis `NOTIFIE_PRESTATAIRE` ; le fournisseur voit l'acte dans son espace ; annulation d'un acte visé ou signé avec `libererAE` (Q2) | UC05, onglet 2 (`signer`, `notifierFournisseur`), onglet 3 (« Notifier le BCA signé ») | 2c |

**2a livré (V17) :**
- **Besoin :** `attribuer` (`EN_EVALUATION → ATTRIBUEE`) et `declarerInfructueux` (`EN_EVALUATION → INFRUCTUEUSE`), avec `dateDecision`.
- **Soumission :** `retenir` (`DEPOSEE → RETENUE`) et `ecarter` (`DEPOSEE → NON_RETENUE`), avec `dateDecision`. L'index unique partiel `uk_soumission_retenue_par_besoin` garantit une seule offre retenue par besoin (attribution globale).
- **Routes**, toutes contrôlées sur le périmètre du besoin :
  - `GET /api/v1/besoins/{id}/soumissions` : liste paginée, triable par montant, délai ou statut ;
  - `GET /api/v1/besoins/{id}/soumissions/{sid}` : détail d'une offre ;
  - `PATCH /api/v1/besoins/{id}/attribuer`, corps `{soumissionId}`, permission `EXPRIMER_BESOIN` ;
  - `PATCH /api/v1/besoins/{id}/infructueux`, permission `EXPRIMER_BESOIN`.
- **Réponses :** `SoumissionResponse` gagne `fournisseurId`, `fournisseurRaisonSociale`, `fournisseurNiu` et `dateDecision`, et `ExpressionBesoinResponse` gagne `dateDecision`. Ce sont des ajouts : aucun champ existant ne change.

**2b livré (V18, V19) :**
- **Taxes (V18, Q23) :** la table `taxes` contient TVA 19,25 % et IR 5,5 %, modifiables par l'admin. `CalculTaxe` → `TvaEtIrParametres` calcule TVA, IR, TTC et NAP de l'offre, et le fournisseur ne saisit plus le TTC.
- **Acte (V19), package `depense/engagement` :**
  - `ActeJuridique` : numéro, type, objet, date d'émission, délai en mois, HT, TVA, IR, TTC, NAP, montant en lettres, statut, besoin, offre retenue, ligne de crédit ;
  - `ActeJuridiqueFactory` (`determinerForme`, `creerActe`) ;
  - `FormeActeStrategie` avec une seule stratégie, `StrategieBonDeCommande` (numérotation Q24, éligibilité toujours vraie tant que la table des seuils est vide) ;
  - `common/texte/MontantEnLettres` (orthographe traditionnelle, vérifiée sur les montants du BCA).
- **Règles :**
  - création à partir d'un besoin `ATTRIBUEE` et de son offre `RETENUE`, un acte par besoin (409 `ACTE_DEJA_CREE`) ;
  - montants, fournisseur, RIB et lignes repris de l'offre ;
  - délai proposé = jours de l'offre ÷ 30, arrondi au supérieur (Q25) ; date d'émission = date de création (Q26) ;
  - seuls l'objet et le délai se modifient, et seulement en `PROJET` (`ACTE_NON_MODIFIABLE`) ;
  - envoi au CF : `PROJET → SOUMIS_CONTROLE`, ligne de crédit déduite de l'imputation du besoin (exercice, section, programme, action de la tâche, chapitre, nature), sinon 400 `CREDIT_IMPUTATION_INTROUVABLE` (Q22). La disponibilité des AE est contrôlée par le CF en 2c.
- **Routes `/api/v1/actes`** (écriture : `ENGAGE_DEPENSE` + périmètre du besoin ; lecture : périmètre) :
  - `POST` : création, avec `besoinId`, `type` facultatif (BCA par défaut) et `delaiLivraisonMois` facultatif ;
  - `GET` : liste paginée, filtres exercice et statut ;
  - `GET /{id}` : contenu complet du BCA (structure émettrice, imputation, fournisseur, RIB, lignes, montants) ;
  - `PUT /{id}` : modification de l'objet et du délai ;
  - `PATCH /{id}/soumettre` : envoi au CF.
- **Bénéficiaire (Q27) :** pas encore ajouté, en attente de vérification.
- **Refonte (V21, demandée le 2026-11-11) : données de la forme séparées de l'acte, par composition.**
  - L'onglet 4 relie l'acte à une `FormeActeStrategie` qui porte ses propres attributs (« applique forme contractuelle », 1 ↔ 1).
  - **Données :** l'entité `BonDeCommande` (table `bons_de_commande`, liée 1-1 à l'acte) contient `delaiLivraisonMois` (retiré de l'acte), `referenceDevis` (facultative), `codeBarresSecurite` et `qrCodeSecurite` (remplis avec l'impression, lot 3).
  - **Comportement :** `StrategieBonDeCommande` numérote l'acte, vérifie l'éligibilité et applique les données (`appliquerDonnees`, `DonneesForme`). Le délai est saisi, sinon conservé, sinon proposé depuis l'offre.
  - **Pourquoi la composition plutôt que l'héritage :** la forme d'un acte peut changer (`changerForme` de l'onglet 4, par exemple après un rejet du CF).
  - **Autres formes :** chacune aura sa table et sa stratégie. `changerForme` sera ajouté quand une deuxième forme existera.
  - **Pas de `delaiExecution` générique sur l'acte :** le délai vit dans les données de chaque forme.
  - **API :** inchangée, plus `referenceDevis` en entrée et en sortie.

**2c livré (V20) :**
- **`Visa`** (package `depense/controle`, onglet 4) : numéro, `typeVisa = ENGAGEMENT_JURIDIQUE`, date de réception (envoi au CF), date du visa, décision (`StatutVisa`), observations, référence du quitus et acte. Le CF auteur est tracé par `createdBy`.
- **`ReservationCredit`** (`depense/engagement`) : numéro, montant, date, statut (`ACTIVE` / `ANNULEE`), acte (un seul) et **ligne de crédit** (lien ajouté, absent des diagrammes).
- **Une seule décision transactionnelle** (Q1), `POST /api/v1/actes/{id}/visas` :
  - `FAVORABLE` :
    - permissions `VISA_CFI`, `RESERVER_CREDIT` et `QUITUS_NATURE` sur le périmètre du besoin ;
    - `consommerAE(TTC)` sur la ligne, refusé si la ligne est bloquée ou si les AE sont insuffisantes : le CF rejette alors l'acte ;
    - réservation enregistrée ;
    - quitus porté par le visa (Q19) ;
    - acte au statut `VISE_CF`.
  - `DEFAVORABLE_REJET` : motif obligatoire, l'acte revient en `PROJET` (ligne de crédit effacée, de nouveau déduite au prochain envoi), rien n'est réservé (Q2, Q18).
  - `SUSPENDU_RESERVES` : motif obligatoire, l'acte reste `SOUMIS_CONTROLE` (Q18).
- **Historique :** `GET /api/v1/actes/{id}/visas`. L'acte affiche le numéro et le montant de sa réservation.
- **Numérotation provisoire** (à confirmer, §7) : `VISA-<année>-00001`, `RES-<année>-00001`. Référence du quitus générée : `QUI-<année>-00001`.

**Mise en œuvre prévue de la notification (Q20, étape 2d) :**
- **Signature :** `signer` fait passer l'acte à `SIGNE_ORDONNATEUR` (signataire, `dateSignature`), puis à `NOTIFIE_PRESTATAIRE` (`dateNotification`) dans la même transaction.
- **Côté fournisseur :** il consulte ses actes notifiés dans son espace (`GET /api/v1/fournisseurs/moi/actes` et le détail). Seuls les actes notifiés y figurent.
- **Pas d'e-mail ni de SMS pour l'instant :** le projet n'a aucun service d'envoi. Une table de notifications (boîte de réception dans l'application) pourra servir plus tard au PV, au paiement, etc.

**Lecture d'un BCA réel** (fourni le 2026-11-11, n° BCA-A602010097) :

| Zone du BCA | Exemple | Source dans GBE | État |
|---|---|---|---|
| N° | BCA-A602010097 | `ActeJuridique.numero` | `BCA-A<EXERCICE><SECTION><TROUVE><N° D'ORDRE>` (Q24, segment « TROUVE » à préciser) |
| Structure émettrice | 33000035 - Division de l'Informatique | chapitre du besoin (code `2033000035` en base) | ✓ |
| Date d'émission | 03/11/2026 | absente du modèle | Q26 |
| Délai de livraison (en mois) | 1 | `delaiLivraisonMois` (onglet 1) / `delaiLivraisonMaxMois` (onglet 4) ; l'offre a `delaiProposeJours` | Q25 |
| Objet | Achat des préimprimés… | objet du besoin | ✓ |
| N° contribuable, raison sociale, adresse, téléphone | M086… | `Fournisseur` (NIU…) | ✓ |
| N° RIB | 10025… | RIB choisi dans l'offre (1e) | ✓ |
| Programme, action, activité, tâche | 233, 7, 202337G011… | tâche du besoin → activité → action → programme | ✓ |
| Imputation budgétaire | 60 20 233 7 33000035 0112 360140 | 60 = exercice (`code_exercice`), 20 = section, 233 = programme, 7 = action, 33000035 = chapitre, 360140 = nature (vérifié en base) ; 0112 = classification fonctionnelle (Q28) | origine de la fonction à définir |
| Bénéficiaire | 11000020 - MINFI | absent du modèle et du référentiel | Q27 |
| Lignes : n°, référence, désignation, qté, P.U., P.T. HT | 34-004-220017… | matière, quantité (besoin) et prix (offre retenue) | ✓ |
| Total HT, TVA (19,25 %), IR (5,5 %), NAP, total TTC | 4 120 000 ; 793 100 ; 226 600 ; 3 893 400 ; 4 913 100 | TVA et IR absents du modèle de l'acte | Q23 |
| Arrêté à la somme de | quatre millions neuf cent treize mille cent | `montantEnLettres` = TTC en lettres | ✓ |
| Code-barres, QR code | — | `codeBarres`, `qrCodeSecurite` (onglet 1) | impression, plus tard |
| Fait à …, le …, l'Ordonnateur | (vierge) | `dateSignature` (Q6) ; lieu absent du modèle | Q29 |
| Talon : visa du CF, RIB, Paierie générale du Trésor | — | visa (2c) ; poste comptable absent du modèle | lot 4 |

Calculs constatés : TVA = HT × 19,25 % ; IR = HT × 5,5 % ; **TTC = HT + TVA** ; **NAP = TTC − TVA − IR** (= HT − IR). Le
montant arrêté en lettres est le TTC. Conséquence pour Q17 : le net à payer n'est pas le TTC.

**Écarts relevés dans la conception (à trancher, voir Q13–Q22) :**
- `ActeJuridique → ReservationCredit` est en **0..1** (onglets 1 et 4) alors que la fiche objet dit « adossé à **1..*** crédits ». Proposition : une seule réservation, sur une seule ligne.
- `ReservationCredit` n'est **relié à aucune ligne de crédit** dans les diagrammes. Il faut ajouter `ReservationCredit → CreditBudgetaire`.
- **Imputation :** UC04 dit que l'ordonnateur « renseigne l'imputation budgétaire prévisionnelle ». Or le besoin porte déjà l'exercice, la section, le programme, la tâche (donc l'action), le chapitre et la nature, c'est-à-dire exactement la clé `uk_credit_imputation`. Proposition : la ligne de crédit est **déduite** du besoin.
- **Quitus :** la fiche objet `QuitusNatureEconomique` (date, code nomenclature, justification, statut) n'existe pas dans l'onglet 4, où seul `Visa.referenceQuitus` le représente. L'onglet 4 relie aussi `Matiere → NatureEconomique` (« classé dans (quitus CF) »), lien écarté au lot 0 : le quitus porte donc sur la **nature du besoin**.
- **`StatutVisa.SUSPENDU_RESERVES`** contre la fiche objet `Visa` : « favorable **avec réserves** ».
- **`StatutActe.REJETE`** contre l'onglet 2 et l'onglet 3, où un rejet **renvoie le dossier** à la préparation du projet d'acte (`rejeter(motif)` → `ATTRIBUEE`).
- **`StatutSoumission`** prévoit `RECEVABLE`, `EN_EXAMEN` et `REJETEE`, mais l'évaluation se fait en commission externe (Q4).
- **Signature :** l'onglet 2 a un seul état `SIGNE_ENGAGE`, et `StatutActe` en a deux (`SIGNE_ORDONNATEUR`, `NOTIFIE_PRESTATAIRE`). UC05 notifie « immédiatement ».
- Les **documents** (`genererModele`, `imprimerBCA`, code-barres et QR code) supposent le stockage de fichiers, prévu au lot 3.

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

**Questions du lot 2** (hypothèse par défaut entre parenthèses) :

| # | Question | Hypothèse | Décision |
|---|---|---|---|
| Q13 | Attribution : que saisit-on du résultat de la commission ? | offre retenue, référence et date du PV de commission (texte, le fichier viendra au lot 3) ; les autres offres passent `NON_RETENUE` ; `RECEVABLE`, `EN_EXAMEN` et `REJETEE` ne sont pas utilisés pour l'instant | **Décidé (2026-11-11)** : l'ordonnateur sélectionne seulement l'offre retenue, rien d'autre n'est saisi (la date est tracée automatiquement). L'offre retenue passe `RETENUE`, les autres `NON_RETENUE`. `RECEVABLE`, `EN_EXAMEN` et `REJETEE` ne sont pas utilisés. |
| Q14 | Quand l'ordonnateur peut-il voir les offres ? | seulement après la clôture (`EN_EVALUATION`) | **Décidé (2026-11-11)** : à tout moment, y compris avant la clôture. À noter : les prix sont visibles de l'ordonnateur pendant la consultation. |
| Q15 | Que deviennent les offres d'un besoin annulé ou infructueux ? | elles restent `DEPOSEE`, figées ; le fournisseur voit le statut du besoin | **Décidé (2026-11-11)** : elles restent `DEPOSEE`, figées. |
| Q16 | Les fournisseurs non retenus sont-ils informés dans l'application (§9) ? | oui, par le statut `NON_RETENUE` de leur offre | **Décidé (2026-11-11)** : oui, par le statut `NON_RETENUE` de leur offre. |
| Q17 | Quel montant réserver sur les AE, et que vaut `montantNetAPayer` au lot 2 ? | le TTC de l'acte (donc de l'offre retenue) ; net à payer = TTC, les retenues viennent au lot 4 | **Décidé (2026-11-11)** : le TTC pour le moment ; **à revoir éventuellement** (montant réservé et net à payer). |
| Q18 | Sens des trois décisions du CF : `FAVORABLE`, `DEFAVORABLE_REJET`, `SUSPENDU_RESERVES` ; à quoi sert `StatutActe.REJETE` ? | rejet → l'acte revient en `PROJET` (corrigeable, comme dans les onglets 2 et 3) ; réserves → l'acte reste `SOUMIS_CONTROLE` en attente de compléments ; `REJETE` n'est pas utilisé | **Décidé (2026-11-11)** : comme l'hypothèse. |
| Q19 | Quitus : objet à part (fiche `QuitusNatureEconomique`) ou porté par le visa (onglet 4, `referenceQuitus`) ? | porté par le visa | **Décidé (2026-11-11)** : porté par le visa. |
| Q20 | Notification : automatique à la signature (UC05) et visible dans l'espace du fournisseur ? | oui : signature puis `NOTIFIE_PRESTATAIRE` dans la même opération, et le fournisseur consulte ses actes | **Décidé (2026-11-11)** : oui. Mise en œuvre proposée en §4.5 (2d). |
| Q21 | Champs propres à chaque forme (référence du devis, n° d'appel d'offres, lot, retenue de garantie, pénalités, dates et loyer du bail) et documents générés | champs facultatifs saisis au lot 2 ; documents (BCA imprimé, modèle) au lot 3, avec le stockage de fichiers | **Décidé (2026-11-11)** : **seule la forme BCA** est traitée jusqu'à la fin du module ; les autres formes viendront plus tard, selon les informations disponibles. Le bas du BCA sera fourni par l'utilisateur pour en fixer les champs. |
| Q22 | Imputation : ligne de crédit déduite du besoin, ou choisie par l'ordonnateur ? | déduite du besoin ; soumission au CF refusée si la ligne n'existe pas | **Décidé (2026-11-11)** : déduite du besoin. |
| Q23 | Taxes du BCA : TVA 19,25 % et IR 5,5 % calculés par le serveur sur le HT (TTC = HT + TVA, NAP = TTC − TVA − IR) ? Le fournisseur cesse-t-il de saisir le TTC dans son offre ? Les taux varient-ils (régime fiscal du fournisseur, exonérations) ? | taux en table de paramétrage ; TTC et NAP calculés par le serveur, à l'offre comme à l'acte ; exonérations plus tard | **Décidé (2026-11-11)** : le serveur calcule les taxes. Taux en table `taxes` (TVA 19,25 %, IR 5,5 %), modifiables par l'admin. L'offre porte TVA, IR, TTC et net à payer, et le fournisseur ne saisit plus le TTC. **Livré (V18).** |
| Q24 | Format du numéro (BCA-A602010097) | provisoire : `BCA-<année>-00001` | **Décidé (2026-11-11)** : `BCA-A` + code de l'exercice (`exercice.code_exercice`, 60 pour 2026) + code de la section (20 pour le MINFI) + numéro d'ordre **sur 5 chiffres**, qui repart à chaque **exercice** sans tenir compte de la section. Exemple : `BCA-A602010097` = A, 60, 20, 10097. **À vérifier** : la portée par exercice seul, et le premier numéro (00001 supposé, « repart à 0 »). Livré en 2b. |
| Q25 | Délai de livraison en mois : saisi par l'ordonnateur au projet, ou déduit du délai de l'offre (jours) ? | saisi au projet, proposé à partir de l'offre (jours ÷ 30, arrondi au supérieur) | **Décidé (2026-11-11)** : comme l'hypothèse. |
| Q26 | Date d'émission : création du projet, envoi au CF ou signature ? | date de création du projet | **Décidé (2026-11-11)** : date de création du projet. |
| Q27 | Bénéficiaire (11000020 - MINFI) : de quoi s'agit-il, d'où vient-il ? | — | **Conception mise à jour (2026-11-11)** : classe `Beneficiaire` (`code`, `libelle`) ajoutée aux onglets 1 et 4 des deux drawio, avec l'association `ExpressionBesoin` → `Beneficiaire` « au profit de (bénéficiaire) ». Absente du rapport et du référentiel (code `11000020` introuvable parmi les chapitres). **Reste à définir avant de coder** : nature du code (chapitre ou autre nomenclature), source des données, multiplicité, valeur par défaut. |
| Q28 | Imputation : sens de « 60 » et origine de « 0112 » (classe fonctionnelle, table vide) | sert seulement à l'impression, peut attendre | **Décidé (2026-11-11)** : `60` = code de l'exercice (2026), `0112` = classification fonctionnelle. Reste : d'où vient la classification fonctionnelle (table `classe_fonctionnelle` vide). Reporté avec l'impression (§7). |
| Q29 | « Fait à » : lieu saisi sur l'acte ou fixé par structure ? | sert à l'impression, peut attendre | Reporté avec l'impression (§7). |
| Q30 | Périmètre du CF : aujourd'hui, seul un mandat MINISTRE sans programme couvre toute la section ; un CF ne voit et ne vise que les actes de son programme. Le CF doit-il couvrir toute une section (ou une « zone de compétence », attribut `zoneCompetence` de la classe `ControleurFinancier`) ? | — | — |

Autres questions du rapport (section 9), pour les lots 3 et 4 : livraisons partielles et paiements partiels, procédure en cas de non-conformité, liste des pièces justificatives obligatoires, rôle exact du comptable public (prise en charge, rejet, statut de paiement), limite du suivi fournisseur.

Point à part : le mandat MINISTRE (section MINFI) n'a aucune permission en base. **Décidé (2026-11-11)** : lui donner les permissions par défaut du rôle, par migration (lot 0).

---

## 7. Reporté (à faire plus tard)

Ce qui a été volontairement laissé de côté, avec sa source et le moment prévu. À relire au début de chaque lot.

### Impression et documents de l'acte (lot 3, avec le stockage de fichiers)
- **BCA imprimé** : `genererModele` / `imprimerBCA` (onglet 4), mise en page du BCA réel (§4.5).
- **Code-barres et QR code** : `codeBarres`, `qrCodeSecurite` (onglet 1, `BonDeCommandeAdministratif`), `codeBarresSecurite` (onglet 4). Leur contenu est à définir.
- **Imputation imprimée complète** `60 20 233 7 33000035 0112 360140` (Q28) :
  - « 60 » est le code de l'exercice (Q28) ;
  - `0112` est la classification fonctionnelle, mais la table `classe_fonctionnelle` est vide et rattachée à rien : il faut définir d'où vient la fonction d'un besoin ou d'une ligne de crédit.
- **« Fait à … »** (Q29) : lieu de signature, saisi sur l'acte ou fixé par structure.
- **Talon du BCA :** case du visa du CF, RIB, « Paierie générale du Trésor ». Ce poste comptable assignataire est absent du modèle et concerne le comptable public (lot 4).
- **Pièces de l'offre** (Q10, écart avec UC03) et éventuel PV de commission en fichier (Q13).

### Formes d'acte autres que le BCA (Q21)
- Lettre-commande, marché public, convention de bail et leurs champs propres (onglet 4 : référence du devis, n° d'appel d'offres, lot, cautions, retenue de garantie, pénalités, dates et loyer du bail).
- Ces formes viendront quand les informations seront disponibles. Jusque-là, seule la forme BCA est traitée, mais l'architecture garde la `FormeActeStrategie` (une stratégie par forme).
- La table des seuils (Q3) reste vide, et le choix de la forme est libre.

### Taxes et montants
- **Taxes selon le type d'acte** (à faire quand les règles de la LC, du marché et du bail seront connues) :
  - colonne `type_acte` dans `taxes`, avec un taux par forme ;
  - `CalculTaxe.calculer(besoin, typeActe, montantHT)`, et chaque `FormeActeStrategie` ajoute ses retenues propres (retenue de garantie du marché…) ;
  - à la création de l'acte, recalcul des taxes avec le type choisi au lieu de la recopie de l'offre : l'acte fait foi (montant réservé, montant en lettres) ;
  - le TTC de l'offre devient indicatif (calculé avec la forme BCA), et les offres se comparent sur le HT ;
  - à décider alors : le type d'acte peut-il être fixé dès la publication du besoin, pour un TTC exact sur l'offre ?
- Méthodes de calcul par type de besoin (§4.4), exonérations, et taux selon le régime fiscal du fournisseur. Aujourd'hui, les mêmes taux TVA et IR (table `taxes`) s'appliquent à toutes les offres (Q23).
- Arrondi : au centime (demi supérieur). Le franc CFA n'a pas de centimes : il faudra peut-être arrondir à l'unité.
- Montant réservé et net à payer (Q17) : décision provisoire « TTC », à revoir ; le BCA montre que NAP = HT − IR.

### Numérotation et référentiel
- Numéros du visa (`VISA-<année>-00001`), de la réservation (`RES-…`) et du quitus (`QUI-…`) : provisoires, à aligner sur les formats réels.
- Numéro du BCA (Q24) : à vérifier que le numéro d'ordre repart bien par exercice seul (toutes sections confondues, compteur `BCA-A<code exercice>`), et que le premier numéro est 00001.
- Bénéficiaire (Q27) : classe `Beneficiaire` ajoutée au drawio (onglets 1 et 4). Pas encore codée : il faut savoir si le code est un chapitre (`<CODE CHAPITRE>-<SIGLE SECTION>` ?) ou une autre nomenclature, et d'où viennent les données.

### Soumissions et fournisseurs
- `StatutSoumission` : `RECEVABLE`, `EN_EXAMEN` et `REJETEE` ne sont pas utilisés (Q13, commission externe).
- Le fournisseur ne gère pas lui-même ses RIB (l'admin le fait). `estActif` et `validerCle()` du RIB (onglet 1) ne sont pas repris (on suit l'onglet 4).
- Les offres sont visibles de l'ordonnateur avant la clôture (Q14) : point de confidentialité à garder en tête.

### Notifications
- Pas d'e-mail ni de SMS : les décisions sont visibles dans l'application (statut des offres, actes notifiés dans l'espace fournisseur, Q20). Une boîte de notifications pourra être ajoutée plus tard (PV, paiement…).

### Sécurité et API
- **401 ou 403 :** une requête sans jeton ou avec un jeton invalide reçoit 403. Un `AuthenticationEntryPoint` renverrait 401, à coordonner avec le frontend.
- **Compte désactivé :** il garde l'accès jusqu'à l'expiration de son jeton d'accès. Le filtre pourrait le refuser tout de suite.
- **Rotation du jeton de rafraîchissement :** le même jeton est renvoyé, sans rotation.
- **Pagination :** les listes de crédits ne sont pas paginées (cela changerait la forme des réponses).

### À signaler au frontend
- Routes `PATCH /credits/{id}/engager` et `/solder` supprimées, avec les statuts `ENGAGE` et `SOLDE`.
- Un refus de permission répond 403 au lieu de 500.
- `GET /api/v1/besoins` renvoie une page (`PageResponse`).
- API fournisseur : `telephoneRepresentant` pour le téléphone du représentant.
- Tags Swagger renommés, ce qui compte si le client est généré à partir des tags. Le passage à `ResponseEntity` ne change rien à l'API.
- Jeton de rafraîchissement invalide : 401 `REFRESH_TOKEN_INVALIDE` au lieu de 500.
- Nouveaux champs (ajouts) dans `SoumissionResponse` et `ExpressionBesoinResponse`.
- Nouvelles routes `/api/v1/actes` (projet d'acte BCA) et `/api/v1/actes/{id}/visas` (décision du CF).
- Offre : le fournisseur ne saisit plus `montantProposeTTC` (ignoré s'il est envoyé). La réponse donne `tauxTva`, `montantTva`, `tauxIr`, `montantIr` et `montantNetAPayer`. Le code d'erreur `MONTANT_TTC_INVALIDE` disparaît.

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
| 2026-11-11 | Tous les contrôleurs retournent `ResponseEntity` (corps JSON inchangé). Renouvellement de jeton : 401 `REFRESH_TOKEN_INVALIDE` au lieu de 500 (jeton invalide, expiré, d'un autre type, ou compte inactif), 400 si absent. |
| 2026-11-11 | Sécurité, corrigé avant le lot 2 : le filtre JWT n'accepte plus que les jetons d'accès (un jeton MFA contournait la double authentification, un jeton de rafraîchissement servait de jeton d'accès). Un jeton invalide rend la requête anonyme (403 sur route protégée) au lieu d'une exception non gérée. |
| 2026-11-11 | Lot 2 : conception relue (UC04–UC08, fiches objet, §9, onglets 1–4). Découpage 2a–2d proposé (§4.5), écarts relevés, questions Q13–Q22 ouvertes. |
| 2026-11-11 | Q13–Q22 décidées (seule la forme BCA jusqu'à la fin). **2a livré** (V17) : attribution d'un besoin à une offre, infructuosité, consultation des offres par l'ordonnateur à tout moment. |
| 2026-11-11 | BCA réel analysé (§4.5) : imputation décodée par le référentiel, taxes TVA 19,25 % et IR 5,5 %, NAP = HT − IR. Questions Q23–Q29 ouvertes avant 2b. |
| 2026-11-11 | Q24 (format du n° BCA), Q25, Q26 décidées ; 0112 = classification fonctionnelle ; Q27 à vérifier. Nouvelle section §7 : tout ce qui est reporté, avec sa source. |
| 2026-11-11 | Q23, Q24 et Q28 (« 60 » = code de l'exercice) décidées. **2b, partie taxes** (V18) : table `taxes` (TVA 19,25 %, IR 5,5 %), `CalculTaxe` → `TvaEtIrParametres`, l'offre porte TVA, IR, TTC et NAP calculés par le serveur. |
| 2026-11-11 | **2b livré** (V19) : projet d'acte BCA (fabrique et stratégie BCA, numéro `BCA-A<exercice><section><ordre>`, montants de l'offre retenue, TTC en lettres), modification en `PROJET`, envoi au CF avec ligne de crédit déduite du besoin. Q24 précisée (ordre par exercice, à vérifier). |
| 2026-11-11 | Les taxes dépendent du type d'acte : seules les règles du BCA sont connues, on fait avec. Évolution notée en §4.4 et §7 (taux par type, recalcul à l'acte, TTC indicatif sur l'offre). |
| 2026-11-11 | **2c livré** (V20) : visa du CF en une décision (favorable : quitus + réservation des AE + `VISE_CF` ; rejet : retour en `PROJET` ; réserves : reste soumis). Question Q30 ouverte (périmètre du CF). |
| 2026-11-11 | Refonte de 2b (V21) : données propres au BCA dans `bons_de_commande` (délai en mois, référence du devis, code-barres et QR code à venir), rattachées à l'acte par composition ; `StrategieBonDeCommande` porte le comportement. |
| 2026-11-11 | Drawio : classe `Beneficiaire` (code, libellé) et association « au profit de (bénéficiaire) » depuis `ExpressionBesoin`, onglets 1 et 4 des deux copies. Pas encore de code (Q27). |
| 2026-11-11 | **Drawio aligné sur le code livré** (onglets 1 et 4, deux copies). Classes mises à jour : `ExpressionBesoin` (sans `montantEstime` ni `valider()` ; dates de publication, de clôture effective et de décision, motif, suppression logique, `publier`, `declarerInfructueux`, `annuler`, `supprimer`), `Soumission` (numéro, TVA, IR, TTC, NAP, empreinte, date de décision, `ecarter`, `sceller`), `LigneSoumission` (sans délai par ligne), `CoordonneeBancaire` (RIB seul), `Matiere` (référence et libellé), `ActeJuridique` (type, date d'émission, TVA, IR, date d'envoi au CF, `viser`, `renvoyerEnProjet` ; délai déplacé dans la forme), BCA (délai en mois, référence du devis, code-barres et QR code), `Visa` de l'onglet 1 aligné sur l'onglet 4, `ReservationCredit` (`StatutReservation`, date d'annulation). Ajouts : classes `CreditBudgetaire` et `Taxe`, liens `Soumission → CoordonneeBancaire` (« payée sur (RIB choisi) »), `ReservationCredit → CreditBudgetaire` (« sur la ligne de crédit »), dépendance `Soumission ⇢ Taxe`, énumérations `StatutReservation` et `CodeTaxe` (copie de `analyse-fonctionnelle/`). Lien `Matiere → NatureEconomique` conservé mais annoté « non relié pour l'instant ». |
