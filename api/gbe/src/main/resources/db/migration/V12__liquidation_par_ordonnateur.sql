-- ============================================================
-- GBE — V12 : la liquidation relève de l'ordonnateur
--
-- Diagramme d'activité (onglet 3) : « Liquider la dépense » est une action de
-- l'ordonnateur. LIQUIDER_DEPENSE quitte les permissions par défaut de COMPTABLE
-- (comptable public : prise en charge et paiement) pour ORDONNATEUR et MINISTRE.
-- Les mandats existants sont alignés. Décision du 2026-11-11.
-- ============================================================

DELETE FROM mandat_permissions mp
USING mandat m
WHERE mp.mandat_id = m.id
  AND m.role_systeme = 'COMPTABLE'
  AND mp.permission = 'LIQUIDER_DEPENSE';

INSERT INTO mandat_permissions (mandat_id, permission)
SELECT m.id, 'LIQUIDER_DEPENSE'
FROM mandat m
WHERE m.role_systeme IN ('ORDONNATEUR', 'MINISTRE')
  AND NOT EXISTS (SELECT 1 FROM mandat_permissions mp
                  WHERE mp.mandat_id = m.id AND mp.permission = 'LIQUIDER_DEPENSE');
