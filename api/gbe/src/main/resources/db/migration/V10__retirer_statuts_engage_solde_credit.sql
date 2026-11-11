-- ============================================================
-- GBE — V10 : retrait des statuts ENGAGE et SOLDE des crédits budgétaires
--
-- Une ligne de crédit finance plusieurs dépenses : l'engagement se lit dans les
-- montants (AE/CP consommés et disponibles), plus dans le statut. Les statuts ne
-- servent qu'à bloquer une ligne (CANTONNE, SUSPENDU, ANNULE).
-- Les crédits ENGAGE ou SOLDE redeviennent DISPONIBLE : leurs montants sont conservés,
-- et un crédit sans disponible ne peut de toute façon plus être consommé.
-- ============================================================

UPDATE credit_budgetaire
SET statut = 'DISPONIBLE'
WHERE statut IN ('ENGAGE', 'SOLDE');

ALTER TABLE credit_budgetaire
    DROP CONSTRAINT IF EXISTS credit_budgetaire_statut_check,
    ADD CONSTRAINT credit_budgetaire_statut_check CHECK (statut IN ('CANTONNE', 'DISPONIBLE', 'SUSPENDU', 'ANNULE'));
