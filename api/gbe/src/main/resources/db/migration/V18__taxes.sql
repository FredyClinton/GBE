-- ============================================================
-- GBE — V18 : taux de taxes et calcul des montants par le serveur (lot 2b, décision Q23)
--
-- D'après un BCA réel (plan §4.5) : TVA = HT × 19,25 % ; IR = HT × 5,5 % ;
-- TTC = HT + TVA ; net à payer (NAP) = TTC − TVA − IR (= HT − IR).
-- Les taux sont paramétrés (table taxes) : l'IR peut dépendre du régime fiscal du fournisseur,
-- les exonérations et les méthodes par type de besoin viendront plus tard (plan §4.4 et §7).
-- L'offre porte désormais le détail des montants ; le fournisseur ne saisit plus le TTC.
-- ============================================================

CREATE TABLE IF NOT EXISTS taxes (
    id                 character varying(255) NOT NULL,
    created_by         character varying(255) NOT NULL,
    created_date       date                   NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by   character varying(255),
    code               character varying(20)  NOT NULL,
    libelle            character varying(255) NOT NULL,
    taux               numeric(5, 2)          NOT NULL,
    CONSTRAINT taxes_pkey PRIMARY KEY (id),
    CONSTRAINT uk_taxe_code UNIQUE (code),
    CONSTRAINT taxes_code_check CHECK (code IN ('TVA', 'IR')),
    CONSTRAINT ck_taxe_taux CHECK (taux >= 0 AND taux < 100)
);

INSERT INTO taxes (id, created_by, created_date, code, libelle, taux)
VALUES (gen_random_uuid()::text, 'SYSTEM', CURRENT_DATE, 'TVA', 'Taxe sur la valeur ajoutée', 19.25),
       (gen_random_uuid()::text, 'SYSTEM', CURRENT_DATE, 'IR', 'Impôt sur le revenu (retenue à la source)', 5.50)
ON CONFLICT (code) DO NOTHING;

ALTER TABLE soumissions
    ADD COLUMN IF NOT EXISTS taux_tva            numeric(5, 2),
    ADD COLUMN IF NOT EXISTS montant_tva         numeric(20, 2),
    ADD COLUMN IF NOT EXISTS taux_ir             numeric(5, 2),
    ADD COLUMN IF NOT EXISTS montant_ir          numeric(20, 2),
    ADD COLUMN IF NOT EXISTS montant_net_a_payer numeric(20, 2);

-- Offres déjà déposées (fonction livrée le 2026-11-11, pas encore en production) : montants recalculés
UPDATE soumissions
SET taux_tva            = 19.25,
    montant_tva         = round(montant_propose_ht * 0.1925, 2),
    taux_ir             = 5.50,
    montant_ir          = round(montant_propose_ht * 0.055, 2),
    montant_propose_ttc = montant_propose_ht + round(montant_propose_ht * 0.1925, 2),
    montant_net_a_payer = montant_propose_ht - round(montant_propose_ht * 0.055, 2)
WHERE montant_tva IS NULL;

ALTER TABLE soumissions
    ALTER COLUMN taux_tva SET NOT NULL,
    ALTER COLUMN montant_tva SET NOT NULL,
    ALTER COLUMN taux_ir SET NOT NULL,
    ALTER COLUMN montant_ir SET NOT NULL,
    ALTER COLUMN montant_net_a_payer SET NOT NULL;
