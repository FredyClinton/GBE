-- ============================================================
-- GBE — V5 : nature économique dans l'imputation d'un crédit
--
-- L'imputation d'un crédit devient :
--   exercice + section + programme + action + chapitre + nature économique
--
-- La colonne reste NULL pour les crédits existants (aucune nature ne peut leur
-- être déduite automatiquement) ; l'API l'exige pour toute nouvelle création.
-- NULLS NOT DISTINCT (PostgreSQL 15+) : deux crédits sans nature sur la même
-- imputation administrative restent interdits, comme avant cette migration.
-- ============================================================

ALTER TABLE credit_budgetaire
    ADD COLUMN IF NOT EXISTS nature_economique_id character varying(255);

ALTER TABLE credit_budgetaire
    DROP CONSTRAINT IF EXISTS fk_credit_nature_economique,
    ADD CONSTRAINT fk_credit_nature_economique
        FOREIGN KEY (nature_economique_id) REFERENCES natures_economiques (id);

CREATE INDEX IF NOT EXISTS idx_credit_nature_economique ON credit_budgetaire (nature_economique_id);

ALTER TABLE credit_budgetaire
    DROP CONSTRAINT IF EXISTS uk_credit_imputation,
    ADD CONSTRAINT uk_credit_imputation UNIQUE NULLS NOT DISTINCT
        (exercice_id, section_id, programme_id, action_id, chapitre_id, nature_economique_id);
