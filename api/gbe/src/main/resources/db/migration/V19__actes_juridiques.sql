-- ============================================================
-- GBE — V19 : projet d'acte juridique (lot 2b du module Dépense, UC04)
--
-- Classe ActeJuridique de l'onglet 4 (numero, objet, montantTotalHT, montantTTC, montantNetAPayer,
-- montantEnLettres, delaiExecution, statut) + type (TypeActe, onglet 1), complétée d'après un BCA réel
-- (plan §4.5) : date d'émission, délai de livraison en mois, TVA et IR (taux et montants).
-- Seule la forme BCA est traitée pour l'instant (décision Q21) ; la contrainte liste déjà tous les TypeActe.
-- Un seul acte par besoin (ExpressionBesoin « donne lieu à » 0..1 ActeJuridique) et par soumission retenue.
-- La ligne de crédit est déduite du besoin à l'envoi au CF (décision Q22).
-- Visa, réservation et signature viendront en 2c et 2d.
-- ============================================================

CREATE TABLE IF NOT EXISTS actes_juridiques (
    id                       character varying(255) NOT NULL,
    created_by               character varying(255) NOT NULL,
    created_date             date                   NOT NULL,
    last_modified_date       timestamp(6) without time zone,
    last_midified_by         character varying(255),
    numero                   character varying(30)  NOT NULL,
    type                     character varying(30)  NOT NULL,
    objet                    text                   NOT NULL,
    date_emission            date                   NOT NULL,
    delai_livraison_mois     integer                NOT NULL,
    montant_total_ht         numeric(20, 2)         NOT NULL,
    taux_tva                 numeric(5, 2)          NOT NULL,
    montant_tva              numeric(20, 2)         NOT NULL,
    taux_ir                  numeric(5, 2)          NOT NULL,
    montant_ir               numeric(20, 2)         NOT NULL,
    montant_ttc              numeric(20, 2)         NOT NULL,
    montant_net_a_payer      numeric(20, 2)         NOT NULL,
    montant_en_lettres       text                   NOT NULL,
    statut                   character varying(30)  NOT NULL,
    date_soumission_controle timestamp(0) without time zone,
    expression_besoin_id     character varying(255) NOT NULL,
    soumission_id            character varying(255) NOT NULL,
    credit_budgetaire_id     character varying(255),
    CONSTRAINT actes_juridiques_pkey PRIMARY KEY (id),
    CONSTRAINT uk_acte_numero UNIQUE (numero),
    CONSTRAINT uk_acte_besoin UNIQUE (expression_besoin_id),
    CONSTRAINT uk_acte_soumission UNIQUE (soumission_id),
    CONSTRAINT actes_juridiques_type_check CHECK (type IN (
        'BON_DE_COMMANDE', 'LETTRE_COMMANDE', 'MARCHE_PUBLIC', 'CONVENTION_BAIL')),
    CONSTRAINT actes_juridiques_statut_check CHECK (statut IN (
        'PROJET', 'SOUMIS_CONTROLE', 'VISE_CF', 'SIGNE_ORDONNATEUR', 'NOTIFIE_PRESTATAIRE', 'REJETE', 'ANNULE')),
    CONSTRAINT ck_acte_delai CHECK (delai_livraison_mois > 0),
    CONSTRAINT ck_acte_montants CHECK (montant_total_ht > 0 AND montant_ttc >= montant_total_ht),
    CONSTRAINT fk_acte_besoin FOREIGN KEY (expression_besoin_id) REFERENCES expressions_besoin (id),
    CONSTRAINT fk_acte_soumission FOREIGN KEY (soumission_id) REFERENCES soumissions (id),
    CONSTRAINT fk_acte_credit FOREIGN KEY (credit_budgetaire_id) REFERENCES credit_budgetaire (id)
);

CREATE INDEX IF NOT EXISTS idx_acte_statut ON actes_juridiques (statut);
