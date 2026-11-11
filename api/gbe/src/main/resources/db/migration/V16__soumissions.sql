-- ============================================================
-- GBE — V16 : soumissions (lot 1e du module Dépense, UC03)
--
-- Classes Soumission / LigneSoumission du modèle cible (onglet 4) :
--   soumissions       : numero, date_soumission, montant_propose_ht, montant_propose_ttc,
--                       delai_propose_jours, statut (StatutSoumission)
--   lignes_soumission : numero_ordre, prix_unitaire_propose, montant_total_ligne_ht,
--                       remarques_techniques, → ligne de besoin
-- Compléments :
--   coordonnee_bancaire_id : RIB choisi au dépôt (onglet 3 : « offre financière, prix unitaire & RIB »)
--   empreinte              : sceau SHA-256 du récépissé (fiche UC03 : « récépissé scellé et horodaté »)
-- Règles : une soumission par fournisseur et par besoin ; offre totale (un prix par ligne de besoin) ;
-- modifiable jusqu'à la date limite (décision Q11) ; pas de retrait ni de pièce jointe (décisions 2026-11-11).
-- ============================================================

CREATE TABLE IF NOT EXISTS soumissions (
    id                          character varying(255) NOT NULL,
    created_by                  character varying(255) NOT NULL,
    created_date                date                   NOT NULL,
    last_modified_date          timestamp(6) without time zone,
    last_midified_by            character varying(255),
    numero                      character varying(30)  NOT NULL,
    date_soumission             timestamp(0) without time zone NOT NULL,
    date_derniere_modification  timestamp(0) without time zone,
    montant_propose_ht          numeric(20, 2)         NOT NULL,
    montant_propose_ttc         numeric(20, 2)         NOT NULL,
    delai_propose_jours         integer                NOT NULL,
    statut                      character varying(30)  NOT NULL,
    empreinte                   character varying(64)  NOT NULL,
    expression_besoin_id        character varying(255) NOT NULL,
    fournisseur_id              character varying(255) NOT NULL,
    coordonnee_bancaire_id      character varying(255) NOT NULL,
    CONSTRAINT soumissions_pkey PRIMARY KEY (id),
    CONSTRAINT uk_soumission_numero UNIQUE (numero),
    CONSTRAINT uk_soumission_besoin_fournisseur UNIQUE (expression_besoin_id, fournisseur_id),
    CONSTRAINT soumissions_statut_check CHECK (statut IN (
        'DEPOSEE', 'RECEVABLE', 'EN_EXAMEN', 'RETENUE', 'NON_RETENUE', 'REJETEE')),
    CONSTRAINT ck_soumission_montants CHECK (montant_propose_ht > 0 AND montant_propose_ttc >= montant_propose_ht),
    CONSTRAINT ck_soumission_delai CHECK (delai_propose_jours > 0),
    CONSTRAINT fk_soumission_besoin FOREIGN KEY (expression_besoin_id) REFERENCES expressions_besoin (id),
    CONSTRAINT fk_soumission_fournisseur FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs (id),
    CONSTRAINT fk_soumission_coordonnee_bancaire FOREIGN KEY (coordonnee_bancaire_id)
        REFERENCES coordonnees_bancaires (id)
);

CREATE INDEX IF NOT EXISTS idx_soumission_fournisseur ON soumissions (fournisseur_id);

CREATE TABLE IF NOT EXISTS lignes_soumission (
    id                     character varying(255) NOT NULL,
    created_by             character varying(255) NOT NULL,
    created_date           date                   NOT NULL,
    last_modified_date     timestamp(6) without time zone,
    last_midified_by       character varying(255),
    numero_ordre           integer                NOT NULL,
    prix_unitaire_propose  numeric(20, 2)         NOT NULL,
    montant_total_ligne_ht numeric(20, 2)         NOT NULL,
    remarques_techniques   text,
    ligne_besoin_id        character varying(255) NOT NULL,
    soumission_id          character varying(255) NOT NULL,
    CONSTRAINT lignes_soumission_pkey PRIMARY KEY (id),
    CONSTRAINT uk_ligne_soumission_ligne_besoin UNIQUE (soumission_id, ligne_besoin_id),
    CONSTRAINT ck_ligne_soumission_prix CHECK (prix_unitaire_propose > 0),
    CONSTRAINT fk_ligne_soumission_ligne_besoin FOREIGN KEY (ligne_besoin_id) REFERENCES lignes_besoin (id),
    CONSTRAINT fk_ligne_soumission_soumission FOREIGN KEY (soumission_id) REFERENCES soumissions (id)
);
