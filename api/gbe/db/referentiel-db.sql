-- ============================================================
-- SGDAI - Schéma des tables référentielles
-- Tables : exercices, sections, chapitres, programmes,
--          natures_economiques
-- Liaisons conservées : uniquement la hiérarchie interne
--   exercice ← section ← chapitre
--   exercice ← section ← programme
--   exercice ← nature_economique
-- ============================================================

-- ========================
-- EXERCICES (racine)
-- ========================

CREATE TABLE exercices (
    id                  VARCHAR(255)    NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    created_date        DATE            NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    actif               BOOLEAN,
    annee               INTEGER         NOT NULL,
    code                INTEGER,
    date_debut          DATE,
    date_fin            DATE,
    CONSTRAINT exercices_pkey     PRIMARY KEY (id),
    CONSTRAINT uq_exercices_annee UNIQUE (annee)
);

-- ========================
-- SECTIONS (→ exercice)
-- ========================

CREATE TABLE sections (
    id                  VARCHAR(255)    NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    created_date        DATE            NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    code                VARCHAR(255),
    code_exercice       VARCHAR(255),
    sigle               VARCHAR(255),
    libelle             TEXT,
    libelle_en          TEXT,
    exercice_id         VARCHAR(255),
    CONSTRAINT sections_pkey         PRIMARY KEY (id),
    CONSTRAINT fk_section_exercice   FOREIGN KEY (exercice_id) REFERENCES exercices (id)
);

-- ========================
-- CHAPITRES (→ section, exercice)
-- ========================

CREATE TABLE chapitres (
    id                  VARCHAR(255)    NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    created_date        DATE            NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    code                VARCHAR(255)    NOT NULL,
    code_chap           VARCHAR(3),
    code_exercice       VARCHAR(255),
    code_section        VARCHAR(255),
    libelle             TEXT            NOT NULL,
    libelle_en          TEXT,
    exercice_id         VARCHAR(255),
    section_id          VARCHAR(255),
    CONSTRAINT chapitres_pkey        PRIMARY KEY (id),
    CONSTRAINT fk_chapitre_exercice  FOREIGN KEY (exercice_id) REFERENCES exercices (id),
    CONSTRAINT fk_chapitre_section   FOREIGN KEY (section_id)  REFERENCES sections  (id)
);

-- ========================
-- PROGRAMMES (→ section, exercice)
-- ========================

CREATE TABLE programmes (
    id                  VARCHAR(255)    NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    created_date        DATE            NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    code                VARCHAR(255),
    code_exercice       VARCHAR(255),
    code_section        VARCHAR(255),
    libelle             TEXT,
    libelle_en          TEXT,
    numero              VARCHAR(255),
    exercice_id         VARCHAR(255),
    section_id          VARCHAR(255),
    CONSTRAINT programmes_pkey       PRIMARY KEY (id),
    CONSTRAINT fk_programme_exercice FOREIGN KEY (exercice_id) REFERENCES exercices (id),
    CONSTRAINT fk_programme_section  FOREIGN KEY (section_id)  REFERENCES sections  (id)
);

-- ========================
-- ACTIONS (→ section, programme, exercice)
-- ========================

CREATE TABLE actions (
    id                  VARCHAR(255)    NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    created_date        DATE            NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    code                VARCHAR(255),
    code_chap           VARCHAR(255),
    code_exercice       VARCHAR(255),
    code_programme      VARCHAR(255),
    code_section        VARCHAR(255),
    libelle             TEXT,
    libelle_en          TEXT,
    numero              VARCHAR(255),
    numero_programme    VARCHAR(255),
    exercice_id         VARCHAR(255),
    programme_id        VARCHAR(255),
    section_id          VARCHAR(255),
    CONSTRAINT actions_pkey          PRIMARY KEY (id),
    CONSTRAINT fk_action_exercice    FOREIGN KEY (exercice_id)  REFERENCES exercices  (id),
    CONSTRAINT fk_action_programme   FOREIGN KEY (programme_id) REFERENCES programmes (id),
    CONSTRAINT fk_action_section     FOREIGN KEY (section_id)   REFERENCES sections   (id)
);

-- ========================
-- NATURES ÉCONOMIQUES (→ exercice)
-- ========================

CREATE TABLE natures_economiques (
    id                  VARCHAR(255)    NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    created_date        DATE            NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    code                VARCHAR(255),
    code_titre          VARCHAR(1),
    code_article        VARCHAR(2),
    code_paragraphe     VARCHAR(3),
    code_rubrique       VARCHAR(10),
    libelle             TEXT            NOT NULL,
    libelle_en          TEXT,
    exercice_id         VARCHAR(255),
    CONSTRAINT natures_economiques_pkey     PRIMARY KEY (id),
    CONSTRAINT fk_nature_eco_exercice       FOREIGN KEY (exercice_id) REFERENCES exercices (id)
);