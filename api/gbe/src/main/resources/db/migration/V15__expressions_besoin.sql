-- ============================================================
-- GBE — V15 : expressions de besoin (lot 1c du module Dépense, UC01)
--
-- Besoin exprimé par l'ordonnateur, sans aucun prix (règle actée, Q8) : matière,
-- quantité et spécifications par ligne (l'unité s'écrit dans les spécifications).
-- Rattachements : une tâche (d'où exercice, section, programme, recopiés pour les
-- filtres et le contrôle de périmètre), un chapitre (structure émettrice) et une
-- nature économique.
-- Cycle de vie (StatutExpression) : BROUILLON → PUBLIEE → EN_EVALUATION, PUBLIEE → ANNULEE ;
-- ATTRIBUEE / INFRUCTUEUSE au lot 2. Un brouillon est supprimé logiquement (supprime).
-- ============================================================

-- Compteurs de références métier (EB-2026-00001, …), incrémentés de façon atomique
CREATE TABLE IF NOT EXISTS compteurs_reference (
    cle    character varying(100) NOT NULL,
    valeur bigint                 NOT NULL,
    CONSTRAINT compteurs_reference_pkey PRIMARY KEY (cle)
);

CREATE TABLE IF NOT EXISTS expressions_besoin (
    id                      character varying(255) NOT NULL,
    created_by              character varying(255) NOT NULL,
    created_date            date                   NOT NULL,
    last_modified_date      timestamp(6) without time zone,
    last_midified_by        character varying(255),
    reference               character varying(30)  NOT NULL,
    objet                   text                   NOT NULL,
    date_expression         date                   NOT NULL,
    date_cloture_soumission timestamp(0) without time zone,
    date_publication        timestamp(0) without time zone,
    date_cloture_effective  timestamp(0) without time zone,
    statut                  character varying(30)  NOT NULL,
    motif_annulation        text,
    supprime                boolean                NOT NULL DEFAULT false,
    tache_id                character varying(255) NOT NULL,
    programme_id            character varying(255) NOT NULL,
    section_id              character varying(255) NOT NULL,
    exercice_id             character varying(255) NOT NULL,
    chapitre_id             character varying(255) NOT NULL,
    nature_economique_id    character varying(255) NOT NULL,
    CONSTRAINT expressions_besoin_pkey PRIMARY KEY (id),
    CONSTRAINT uk_expression_besoin_reference UNIQUE (reference),
    CONSTRAINT expressions_besoin_statut_check CHECK (statut IN (
        'BROUILLON', 'PUBLIEE', 'EN_EVALUATION', 'ATTRIBUEE', 'ANNULEE', 'INFRUCTUEUSE')),
    CONSTRAINT fk_expression_besoin_tache FOREIGN KEY (tache_id) REFERENCES taches (id),
    CONSTRAINT fk_expression_besoin_programme FOREIGN KEY (programme_id) REFERENCES programmes (id),
    CONSTRAINT fk_expression_besoin_section FOREIGN KEY (section_id) REFERENCES sections (id),
    CONSTRAINT fk_expression_besoin_exercice FOREIGN KEY (exercice_id) REFERENCES exercice (id),
    CONSTRAINT fk_expression_besoin_chapitre FOREIGN KEY (chapitre_id) REFERENCES chapitres (id),
    CONSTRAINT fk_expression_besoin_nature FOREIGN KEY (nature_economique_id) REFERENCES natures_economiques (id)
);

CREATE INDEX IF NOT EXISTS idx_expression_besoin_statut ON expressions_besoin (statut, date_cloture_soumission);
CREATE INDEX IF NOT EXISTS idx_expression_besoin_programme ON expressions_besoin (programme_id, exercice_id);

CREATE TABLE IF NOT EXISTS lignes_besoin (
    id                   character varying(255) NOT NULL,
    created_by           character varying(255) NOT NULL,
    created_date         date                   NOT NULL,
    last_modified_date   timestamp(6) without time zone,
    last_midified_by     character varying(255),
    numero_ordre         integer                NOT NULL,
    quantite_demandee    numeric(18, 3)         NOT NULL,
    specifications       text,
    matiere_id           character varying(255) NOT NULL,
    expression_besoin_id character varying(255) NOT NULL,
    CONSTRAINT lignes_besoin_pkey PRIMARY KEY (id),
    CONSTRAINT uk_ligne_besoin_numero UNIQUE (expression_besoin_id, numero_ordre),
    CONSTRAINT uk_ligne_besoin_matiere UNIQUE (expression_besoin_id, matiere_id),
    CONSTRAINT ck_ligne_besoin_quantite CHECK (quantite_demandee > 0),
    CONSTRAINT fk_ligne_besoin_matiere FOREIGN KEY (matiere_id) REFERENCES matieres (id),
    CONSTRAINT fk_ligne_besoin_expression FOREIGN KEY (expression_besoin_id) REFERENCES expressions_besoin (id)
);
