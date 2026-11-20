-- Le code d'une action devient son seul numéro : '60.181.2 ' (préfixe, programme, numéro, espace final) devient '2',
-- comme le prévoit le code d'imputation : codeExercice(2) + codeSection(2) + codeProgramme(3) + codeAction(1) + ...
-- Le code est unique dans son programme (aucun doublon programme + numéro dans les données 2026).
-- Le code d'imputation des crédits existants est aligné dans la même migration (avant la mise à jour des actions).
-- Idempotent : seules les lignes encore au format « 60.xxx.n » sont touchées.

-- 1. Crédits : remplacer l'ancien code de l'action par son numéro dans le code d'imputation.
UPDATE credit_budgetaire c
SET code_imputation = replace(c.code_imputation, a.code, trim(a.numero))
FROM actions a
WHERE c.action_id = a.id
  AND a.code LIKE '60.%'
  AND position(a.code IN c.code_imputation) > 0;

-- 2. Actions.
UPDATE actions
SET code = trim(numero)
WHERE code LIKE '60.%'
  AND numero IS NOT NULL
  AND trim(numero) <> '';
