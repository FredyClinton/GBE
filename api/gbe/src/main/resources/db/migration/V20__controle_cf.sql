-- ============================================================
-- GBE — V20 : contrôle du CF sur l'acte juridique (lot 2c, UC06 « include » UC07, UC08)
--
-- Une seule décision transactionnelle (Q1) enregistrée dans un visa (onglet 4 : numero, typeVisa, dateReception,
-- dateVisa, decision, observations, referenceQuitus) :
--   FAVORABLE          : quitus porté par le visa (Q19) + réservation des AE (TTC de l'acte, Q17) + acte VISE_CF
--   DEFAVORABLE_REJET  : motif obligatoire, l'acte revient en PROJET, rien n'est réservé (Q2, Q18)
--   SUSPENDU_RESERVES  : motif obligatoire, l'acte reste SOUMIS_CONTROLE (Q18)
-- Un acte peut recevoir plusieurs visas (« visé par » 0..*) ; il bloque au plus une réservation (0..1).
-- ReservationCredit est relié à la ligne de crédit (lien absent des diagrammes, plan §4.5).
-- ============================================================

CREATE TABLE IF NOT EXISTS visas (
    id                 character varying(255) NOT NULL,
    created_by         character varying(255) NOT NULL,
    created_date       date                   NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by   character varying(255),
    numero             character varying(30)  NOT NULL,
    type_visa          character varying(30)  NOT NULL,
    date_reception     timestamp(0) without time zone NOT NULL,
    date_visa          timestamp(0) without time zone NOT NULL,
    decision           character varying(30)  NOT NULL,
    observations       text,
    reference_quitus   character varying(30),
    acte_juridique_id  character varying(255) NOT NULL,
    CONSTRAINT visas_pkey PRIMARY KEY (id),
    CONSTRAINT uk_visa_numero UNIQUE (numero),
    CONSTRAINT visas_type_visa_check CHECK (type_visa IN ('ENGAGEMENT_JURIDIQUE', 'ORDONNANCEMENT_PAIEMENT')),
    CONSTRAINT visas_decision_check CHECK (decision IN ('FAVORABLE', 'DEFAVORABLE_REJET', 'SUSPENDU_RESERVES')),
    CONSTRAINT ck_visa_motif CHECK (decision = 'FAVORABLE' OR observations IS NOT NULL),
    CONSTRAINT ck_visa_quitus CHECK (decision <> 'FAVORABLE' OR reference_quitus IS NOT NULL),
    CONSTRAINT fk_visa_acte FOREIGN KEY (acte_juridique_id) REFERENCES actes_juridiques (id)
);

CREATE INDEX IF NOT EXISTS idx_visa_acte ON visas (acte_juridique_id);

CREATE TABLE IF NOT EXISTS reservations_credit (
    id                   character varying(255) NOT NULL,
    created_by           character varying(255) NOT NULL,
    created_date         date                   NOT NULL,
    last_modified_date   timestamp(6) without time zone,
    last_midified_by     character varying(255),
    numero               character varying(30)  NOT NULL,
    montant              numeric(20, 2)         NOT NULL,
    date_reservation     timestamp(0) without time zone NOT NULL,
    statut               character varying(30)  NOT NULL,
    date_annulation      timestamp(0) without time zone,
    acte_juridique_id    character varying(255) NOT NULL,
    credit_budgetaire_id character varying(255) NOT NULL,
    CONSTRAINT reservations_credit_pkey PRIMARY KEY (id),
    CONSTRAINT uk_reservation_numero UNIQUE (numero),
    CONSTRAINT uk_reservation_acte UNIQUE (acte_juridique_id),
    CONSTRAINT reservations_credit_statut_check CHECK (statut IN ('ACTIVE', 'ANNULEE')),
    CONSTRAINT ck_reservation_montant CHECK (montant > 0),
    CONSTRAINT fk_reservation_acte FOREIGN KEY (acte_juridique_id) REFERENCES actes_juridiques (id),
    CONSTRAINT fk_reservation_credit FOREIGN KEY (credit_budgetaire_id) REFERENCES credit_budgetaire (id)
);

CREATE INDEX IF NOT EXISTS idx_reservation_credit ON reservations_credit (credit_budgetaire_id);
