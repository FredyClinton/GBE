-- ============================================================
-- GBE — V13 : fournisseurs et coordonnées bancaires (lot 1b du module Dépense)
--
-- Le fournisseur est un utilisateur externe (rôle FOURNISSEUR, aucun mandat),
-- créé par l'administrateur. Son compte (email, téléphone, mot de passe) est dans users.
-- Coordonnées bancaires : seul le RIB est stocké pour le moment (23 caractères :
-- banque 5 + guichet 5 + compte 11 + clé 2). Un fournisseur peut en avoir plusieurs :
-- il choisira au dépôt de sa soumission celui sur lequel il sera payé.
-- ============================================================

CREATE TABLE IF NOT EXISTS fournisseurs (
    id                 character varying(255) NOT NULL,
    created_by         character varying(255) NOT NULL,
    created_date       date                   NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by   character varying(255),
    raison_sociale     character varying(255) NOT NULL,
    niu                character varying(50)  NOT NULL,
    rccm               character varying(100),
    adresse            text,
    user_id            character varying(255) NOT NULL,
    CONSTRAINT fournisseurs_pkey PRIMARY KEY (id),
    CONSTRAINT uk_fournisseur_niu UNIQUE (niu),
    CONSTRAINT uk_fournisseur_user UNIQUE (user_id),
    CONSTRAINT fk_fournisseur_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS coordonnees_bancaires (
    id                 character varying(255) NOT NULL,
    created_by         character varying(255) NOT NULL,
    created_date       date                   NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by   character varying(255),
    rib                character varying(23)  NOT NULL,
    fournisseur_id     character varying(255) NOT NULL,
    CONSTRAINT coordonnees_bancaires_pkey PRIMARY KEY (id),
    CONSTRAINT uk_coordonnee_bancaire_fournisseur_rib UNIQUE (fournisseur_id, rib),
    CONSTRAINT fk_coordonnee_bancaire_fournisseur FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs (id)
);
