-- ============================================================
-- GBE — V4 : unicité de l'imputation d'un crédit budgétaire
--
-- La contrainte uk_credit_imputation déclarée sur l'entité référençait une
-- colonne inexistante (NATURE_ECONOMIQUE_ID) : elle n'a jamais été créée par
-- Hibernate, et deux crédits pouvaient partager la même imputation.
-- Elle est rétablie sur les 5 colonnes que le service vérifie déjà.
--
-- Idempotente : les bases créées avant cette erreur possèdent peut-être déjà
-- la contrainte.
-- ============================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_credit_imputation') THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM credit_budgetaire
        GROUP BY exercice_id, section_id, programme_id, action_id, chapitre_id
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'Crédits budgétaires en double sur (exercice, section, programme, action, chapitre) : '
            'dédoublonner la table credit_budgetaire avant d''appliquer V4';
    END IF;

    ALTER TABLE credit_budgetaire
        ADD CONSTRAINT uk_credit_imputation
        UNIQUE (exercice_id, section_id, programme_id, action_id, chapitre_id);
END $$;
