-- ============================================================
-- GBE — V11 : référentiels Activité et Tâche (lot 1a du module Dépense)
--
-- Hiérarchie programmatique (onglet 4 du modèle) :
--   Programme → Action → Activité → Tâche   (chaque lien 1 → 1..*)
-- L'expression de besoin s'inscrira dans une tâche.
--
-- Comme pour actions, section_id, programme_id et exercice_id sont recopiés
-- (déduits du parent par l'API) pour des filtres homogènes. Un code est unique
-- au sein de son parent.
-- ============================================================

CREATE TABLE IF NOT EXISTS activites (
    id                 character varying(255) NOT NULL,
    created_by         character varying(255) NOT NULL,
    created_date       date                   NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by   character varying(255),
    code               character varying(50)  NOT NULL,
    sigle              character varying(50),
    libelle            text                   NOT NULL,
    action_id          character varying(255) NOT NULL,
    programme_id       character varying(255) NOT NULL,
    section_id         character varying(255),
    exercice_id        character varying(255),
    CONSTRAINT activites_pkey PRIMARY KEY (id),
    CONSTRAINT uk_activite_action_code UNIQUE (action_id, code),
    CONSTRAINT fk_activite_action FOREIGN KEY (action_id) REFERENCES actions (id),
    CONSTRAINT fk_activite_programme FOREIGN KEY (programme_id) REFERENCES programmes (id),
    CONSTRAINT fk_activite_section FOREIGN KEY (section_id) REFERENCES sections (id),
    CONSTRAINT fk_activite_exercice FOREIGN KEY (exercice_id) REFERENCES exercice (id)
);

CREATE INDEX IF NOT EXISTS idx_activite_programme ON activites (programme_id);
CREATE INDEX IF NOT EXISTS idx_activite_exercice ON activites (exercice_id);

CREATE TABLE IF NOT EXISTS taches (
    id                 character varying(255) NOT NULL,
    created_by         character varying(255) NOT NULL,
    created_date       date                   NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by   character varying(255),
    code               character varying(50)  NOT NULL,
    sigle              character varying(50),
    libelle            text                   NOT NULL,
    activite_id        character varying(255) NOT NULL,
    action_id          character varying(255) NOT NULL,
    programme_id       character varying(255) NOT NULL,
    section_id         character varying(255),
    exercice_id        character varying(255),
    CONSTRAINT taches_pkey PRIMARY KEY (id),
    CONSTRAINT uk_tache_activite_code UNIQUE (activite_id, code),
    CONSTRAINT fk_tache_activite FOREIGN KEY (activite_id) REFERENCES activites (id),
    CONSTRAINT fk_tache_action FOREIGN KEY (action_id) REFERENCES actions (id),
    CONSTRAINT fk_tache_programme FOREIGN KEY (programme_id) REFERENCES programmes (id),
    CONSTRAINT fk_tache_section FOREIGN KEY (section_id) REFERENCES sections (id),
    CONSTRAINT fk_tache_exercice FOREIGN KEY (exercice_id) REFERENCES exercice (id)
);

CREATE INDEX IF NOT EXISTS idx_tache_programme ON taches (programme_id);
CREATE INDEX IF NOT EXISTS idx_tache_exercice ON taches (exercice_id);
