-- ============================================================
-- SGDAI - Script de création du schéma de base de données
-- Généré depuis la base distante Render (PostgreSQL 18)
-- Date : 2026-04-07
-- ============================================================

-- ========================
-- TABLES SANS DÉPENDANCES
-- ========================

CREATE TABLE exercices (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    actif               BOOLEAN,
    annee               INTEGER             NOT NULL,
    code                INTEGER,
    date_debut          DATE,
    date_fin            DATE,
    CONSTRAINT exercices_pkey       PRIMARY KEY (id),
    CONSTRAINT uq_exercices_annee   UNIQUE (annee)
);

CREATE TABLE bordereaux (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    date_seance         DATE,
    numero              VARCHAR(255),
    statut              VARCHAR(255),
    CONSTRAINT bordereaux_pkey PRIMARY KEY (id)
);

CREATE TABLE demandeurs (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    nom                 VARCHAR(255),
    sigle               VARCHAR(255),
    CONSTRAINT demandeurs_pkey PRIMARY KEY (id)
);

CREATE TABLE operations (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    nom                 VARCHAR(255),
    CONSTRAINT operations_pkey PRIMARY KEY (id)
);

CREATE TABLE roles (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    name                VARCHAR(255),
    CONSTRAINT roles_pkey PRIMARY KEY (id)
);

CREATE TABLE sources_financement (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    type                VARCHAR(255),
    CONSTRAINT sources_financement_pkey PRIMARY KEY (id)
);

CREATE TABLE users (
    id                      VARCHAR(255)    NOT NULL,
    created_date            DATE            NOT NULL,
    credentials_expired     BOOLEAN,
    date_of_birth           DATE,
    email                   VARCHAR(255)    NOT NULL,
    is_email_verified       BOOLEAN,
    is_enabled              BOOLEAN,
    is_credentials_expired  BOOLEAN,
    first_name              VARCHAR(255)    NOT NULL,
    last_modified_date      TIMESTAMP,
    last_name               VARCHAR(255)    NOT NULL,
    is_account_locked       BOOLEAN,
    mfa_enabled             BOOLEAN         NOT NULL,
    password                VARCHAR(255)    NOT NULL,
    phone_number            VARCHAR(255)    NOT NULL,
    phone_verified          BOOLEAN,
    secret                  VARCHAR(255),
    CONSTRAINT users_pkey               PRIMARY KEY (id),
    CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
    CONSTRAINT uk9q63snka3mdh91as4io72espi UNIQUE (phone_number)
);

-- ========================
-- TABLES DÉPENDANT DE exercices
-- ========================

CREATE TABLE sections (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    abbrev              VARCHAR(255),
    bdg_annee_id        INTEGER,
    code                VARCHAR(255),
    code_budget         VARCHAR(10),
    code_exercice       VARCHAR(255),
    code_mille          VARCHAR(255),
    libelle             VARCHAR(255),
    libelle_en          VARCHAR(255),
    old_id              INTEGER,
    sigle               VARCHAR(255),
    exercice_id         VARCHAR(255),
    CONSTRAINT section_pkey             PRIMARY KEY (id),
    CONSTRAINT fkefoxayy5ulc8wmxacprw54gbs FOREIGN KEY (exercice_id) REFERENCES exercices (id)
);

-- ========================
-- TABLES DÉPENDANT DE sections + exercices
-- ========================

CREATE TABLE chapitres (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    bdg_annee_id        INTEGER,
    bdg_chap_id         INTEGER,
    code                VARCHAR(255)        NOT NULL,
    code_budget         VARCHAR(10),
    code_chap           VARCHAR(3),
    code_exercice       VARCHAR(255),
    code_mille          VARCHAR(3),
    code_section        VARCHAR(255),
    libelle             TEXT                NOT NULL,
    libelle_en          TEXT,
    old_id              INTEGER,
    old_section_old_id  INTEGER,
    exercice_id         VARCHAR(255),
    section_id          VARCHAR(255),
    CONSTRAINT chapitre_pkey        PRIMARY KEY (id),
    CONSTRAINT fk_chapitre_exercice FOREIGN KEY (exercice_id) REFERENCES exercices (id),
    CONSTRAINT fk_chapitre_section  FOREIGN KEY (section_id)  REFERENCES sections  (id)
);

CREATE TABLE programmes (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    autre_code          VARCHAR(255),
    bdg_annee_id        INTEGER,
    bdg_chap_id         INTEGER,
    code                VARCHAR(255),
    code_budget         VARCHAR(10),
    code_chap           VARCHAR(255),
    code_exercice       VARCHAR(255),
    code_mille          VARCHAR(255),
    code_section        VARCHAR(255),
    libelle             TEXT,
    libelle_en          TEXT,
    numero              VARCHAR(255),
    old_id              INTEGER,
    old_section_old_id  INTEGER,
    exercice_id         VARCHAR(255),
    section_id          VARCHAR(255),
    CONSTRAINT programme_pkey        PRIMARY KEY (id),
    CONSTRAINT fk_programme_exercice FOREIGN KEY (exercice_id) REFERENCES exercices (id),
    CONSTRAINT fk_programme_section  FOREIGN KEY (section_id)  REFERENCES sections  (id)
);

-- ========================
-- TABLES DÉPENDANT DE sections + programmes + exercices
-- ========================

CREATE TABLE actions (
    id                      VARCHAR(255)    NOT NULL,
    created_by              VARCHAR(255)    NOT NULL,
    created_date            DATE            NOT NULL,
    last_modified_date      TIMESTAMP,
    last_midified_by        VARCHAR(255),
    autre_code              VARCHAR(255),
    autre_code_prog         VARCHAR(255),
    bdg_annee_id            INTEGER,
    bdg_chap_id             INTEGER,
    bdg_prog_id             INTEGER,
    code                    VARCHAR(255),
    code_budget             VARCHAR(10),
    code_chap               VARCHAR(255),
    code_exercice           VARCHAR(255),
    code_mille              VARCHAR(255),
    code_prog               VARCHAR(255),
    code_programme          VARCHAR(255),
    code_section            VARCHAR(255),
    libelle                 TEXT,
    libelle_en              TEXT,
    numero                  VARCHAR(255),
    numero_programme        VARCHAR(255),
    old_id                  INTEGER,
    old_programme_old_id    INTEGER,
    old_section_old_id      INTEGER,
    exercice_id             VARCHAR(255),
    programme_id            VARCHAR(255),
    section_id              VARCHAR(255),
    CONSTRAINT action_pkey          PRIMARY KEY (id),
    CONSTRAINT fk_action_exercice   FOREIGN KEY (exercice_id)  REFERENCES exercices  (id),
    CONSTRAINT fk_action_programme  FOREIGN KEY (programme_id) REFERENCES programmes (id),
    CONSTRAINT fk_action_section    FOREIGN KEY (section_id)   REFERENCES sections   (id)
);

-- ========================
-- TABLE CENTRALE : dossiers
-- ========================

CREATE TABLE dossiers (
    id                      VARCHAR(255)    NOT NULL,
    created_by              VARCHAR(255)    NOT NULL,
    created_date            DATE            NOT NULL,
    last_modified_date      TIMESTAMP,
    last_midified_by        VARCHAR(255),
    date_courrier           DATE,
    date_instructions_ministre DATE,
    instructions_ministre   VARCHAR(255),
    montant                 NUMERIC,
    nom_ministre            VARCHAR(255),
    objet                   VARCHAR(255),
    reference_courrier      VARCHAR(255),
    statut                  VARCHAR(255),
    suivi_budgetaire        BOOLEAN         NOT NULL,
    -- Avis ST
    st_date_avis            DATE,
    st_eligible             BOOLEAN,
    st_motivation           VARCHAR(255),
    st_source_fi_id         VARCHAR(255),
    st_section_pec_id       VARCHAR(255),
    st_programme_pec_id     VARCHAR(255),
    st_chapitre_pec_id      VARCHAR(255),
    -- Avis COM
    com_date_avis           DATE,
    com_eligible            BOOLEAN,
    com_montant             NUMERIC,
    com_motivation          VARCHAR(255),
    com_source_fi_id        VARCHAR(255),
    com_section_pec_id      VARCHAR(255),
    com_programme_pec_id    VARCHAR(255),
    com_chapitre_pec_id     VARCHAR(255),
    com_section_credit_id   VARCHAR(255),
    com_programme_credit_id VARCHAR(255),
    com_chapitre_credit_id  VARCHAR(255),
    com_action_credit_id    VARCHAR(255),
    -- Relations
    bordereau_id            VARCHAR(255),
    chapitre_id             VARCHAR(255),
    demandeur_id            VARCHAR(255),
    exercice_id             VARCHAR(255),
    operation_id            VARCHAR(255),
    programme_id            VARCHAR(255),
    section_id              VARCHAR(255),
    CONSTRAINT dossiers_pkey            PRIMARY KEY (id),
    CONSTRAINT fk_dossier_exercice      FOREIGN KEY (exercice_id)           REFERENCES exercices           (id),
    CONSTRAINT fk_dossier_section       FOREIGN KEY (section_id)            REFERENCES sections            (id),
    CONSTRAINT fk_dossier_programme     FOREIGN KEY (programme_id)          REFERENCES programmes          (id),
    CONSTRAINT fk_dossier_chapitre      FOREIGN KEY (chapitre_id)           REFERENCES chapitres           (id),
    CONSTRAINT fk_dossier_demandeur     FOREIGN KEY (demandeur_id)          REFERENCES demandeurs          (id),
    CONSTRAINT fkaucw6jq8c2cemfu5dqb596hm3 FOREIGN KEY (operation_id)      REFERENCES operations          (id),
    CONSTRAINT fk3fra7ef36p4wdsvkjn6b3je62 FOREIGN KEY (bordereau_id)      REFERENCES bordereaux          (id),
    -- ST
    CONSTRAINT fkjxsr76xxqmaisvqngof2icvy  FOREIGN KEY (st_source_fi_id)   REFERENCES sources_financement (id),
    CONSTRAINT fk1e31hnus33dcehbx9wvnselen FOREIGN KEY (st_section_pec_id) REFERENCES sections            (id),
    CONSTRAINT fkspba0jg2j3v5m97ks1570joai FOREIGN KEY (st_programme_pec_id) REFERENCES programmes        (id),
    CONSTRAINT fklc5s5na8xudqxqi4obaefgni4 FOREIGN KEY (st_chapitre_pec_id) REFERENCES chapitres          (id),
    -- COM PEC
    CONSTRAINT fk3u2cfc1ki91wjc02wm5gx8i3p FOREIGN KEY (com_source_fi_id)  REFERENCES sources_financement (id),
    CONSTRAINT fkgiqxhxdeluungu3mjpbbqwob3 FOREIGN KEY (com_section_pec_id) REFERENCES sections           (id),
    CONSTRAINT fkh9a540kiuljd5s6c4hm2go860 FOREIGN KEY (com_programme_pec_id) REFERENCES programmes       (id),
    CONSTRAINT fk3y22psg7pqu19xnpxtuo9vqq2 FOREIGN KEY (com_chapitre_pec_id) REFERENCES chapitres         (id),
    -- COM CREDIT
    CONSTRAINT fkfeevatlqohggownskj20j0rok FOREIGN KEY (com_section_credit_id) REFERENCES sections        (id),
    CONSTRAINT fked060kuwimnva3awf460t2fho FOREIGN KEY (com_programme_credit_id) REFERENCES programmes    (id),
    CONSTRAINT fkf3yhcj4263f0pphi5u7gis30y FOREIGN KEY (com_chapitre_credit_id) REFERENCES chapitres      (id),
    CONSTRAINT fkcf77eelqpnrdt5whmk4qd7hpf FOREIGN KEY (com_action_credit_id) REFERENCES actions         (id)
);

-- ========================
-- TABLES DÉPENDANT DE dossiers
-- ========================

CREATE TABLE fichiers_dossier (
    id                  VARCHAR(255)        NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_date        DATE                NOT NULL,
    last_modified_date  TIMESTAMP,
    last_midified_by    VARCHAR(255),
    chemin_stockage     VARCHAR(255)        NOT NULL,
    nom_original        VARCHAR(255)        NOT NULL,
    nom_stockage        VARCHAR(255)        NOT NULL,
    taille              BIGINT,
    type_contenu        VARCHAR(255),
    dossier_id          VARCHAR(255),
    CONSTRAINT fichiers_dossier_pkey    PRIMARY KEY (id),
    CONSTRAINT fkovjk6nckyyewf1y0ahs5pfl3i FOREIGN KEY (dossier_id) REFERENCES dossiers (id)
);

-- ========================
-- TABLE DE JOINTURE
-- ========================

CREATE TABLE users_roles (
    users_id    VARCHAR(255)    NOT NULL,
    roles_id    VARCHAR(255)    NOT NULL,
    CONSTRAINT fkml90kef4w2jy7oxyqv742tsfc FOREIGN KEY (users_id) REFERENCES users (id),
    CONSTRAINT fka62j07k5mhgifpp955h37ponj FOREIGN KEY (roles_id) REFERENCES roles (id)
);