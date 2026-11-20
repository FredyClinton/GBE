-- Les codes de programme ne portent plus le préfixe « 60. » : '60.181' devient '181'
-- (3 chiffres, comme le prévoit le code d'imputation : codeProgramme(3)).
-- Les copies du code sont alignées dans la même migration :
--   * credit_budgetaire.code_imputation (construit avec programmes.code) ;
--   * actions.code_programme (copie de programmes.code à la création / modification d'une action).
-- Le code propre de l'action (actions.code, ex. '60.181.2') n'est pas modifié ici.
-- Idempotent : seules les lignes encore préfixées sont touchées.

-- 1. Crédits : remplacer l'ancien code du programme dans le code d'imputation (avant de changer programmes.code).
UPDATE credit_budgetaire c
SET code_imputation = replace(c.code_imputation, p.code, substring(p.code FROM 4))
FROM programmes p
WHERE c.programme_id = p.id
  AND p.code LIKE '60.%'
  AND position(p.code IN c.code_imputation) > 0;

-- 2. Actions : copie du code du programme.
UPDATE actions
SET code_programme = substring(code_programme FROM 4)
WHERE code_programme LIKE '60.%';

-- 3. Programmes.
UPDATE programmes
SET code = substring(code FROM 4)
WHERE code LIKE '60.%';
