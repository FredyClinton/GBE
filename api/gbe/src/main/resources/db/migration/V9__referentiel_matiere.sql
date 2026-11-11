-- ============================================================
-- GBE — V9 : catalogue des matières (lot 0 du module Dépense)
--
-- Simple catalogue des biens et services sur lesquels porte une expression de besoin.
-- Pas de lien avec la nature économique : une même matière peut relever de plusieurs
-- natures selon le contexte (classement à définir plus tard).
-- ============================================================

CREATE TABLE IF NOT EXISTS matieres (
    id                 character varying(255) NOT NULL,
    created_by         character varying(255) NOT NULL,
    created_date       date                   NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by   character varying(255),
    reference          character varying(100) NOT NULL,
    libelle            text                   NOT NULL,
    CONSTRAINT matieres_pkey PRIMARY KEY (id),
    CONSTRAINT uk_matiere_reference UNIQUE (reference)
);
