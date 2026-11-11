-- ============================================================
-- GBE — V7 : rattachement des natures économiques à l'exercice 2026
--
-- Les natures chargées par V2 n'ont ni exercice_id ni code_exercice :
-- /api/v1/referentiel/exercice/{id}/natures-economiques renvoyait une liste vide.
-- Elles proviennent de la nomenclature 2026 ; seules les natures non rattachées
-- sont modifiées (migration rejouable sans effet).
-- ============================================================

UPDATE natures_economiques n
SET exercice_id   = e.id,
    code_exercice = e.code_exercice::text
FROM exercice e
WHERE e.annee = 2026
  AND n.exercice_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_nature_economique_exercice ON natures_economiques (exercice_id);
