-- ============================================================
-- GBE — V25 : permission SIGNER_ACTE (UC05, décision 2026-11-18)
--
-- La signature de l'acte juridique (VISE_CF → SIGNE_ORDONNATEUR → NOTIFIE_PRESTATAIRE) a sa propre permission,
-- au lieu de ENGAGE_DEPENSE. Permission par défaut des rôles ORDONNATEUR et MINISTRE.
--    - la contrainte CHECK de mandat_permissions est reconstruite avec la nouvelle valeur ;
--    - les mandats ORDONNATEUR et MINISTRE existants la reçoivent ;
--    - le super-admin initial (V3, « toutes permissions ») la reçoit.
-- Migration idempotente. Doit rester alignée sur RoleSysteme et Permission.
-- ============================================================

ALTER TABLE mandat_permissions
    DROP CONSTRAINT IF EXISTS mandat_permissions_permission_check,
    ADD CONSTRAINT mandat_permissions_permission_check CHECK (permission IN (
        'ENGAGE_DEPENSE', 'REVISER_AE', 'REVISER_CP', 'REJETER_DEPENSE', 'VISA_CFI', 'REJETER_CFI',
        'LIQUIDER_DEPENSE', 'PAYER_DEPENSE', 'INSCRIRE_CREDIT', 'MANAGE_USERS', 'MANAGE_AFFECTATIONS', 'CONSULTER',
        'EXPRIMER_BESOIN', 'SOUMISSIONNER', 'RESERVER_CREDIT', 'QUITUS_NATURE', 'RECEPTIONNER_LIVRAISON',
        'ORDONNANCER', 'VISER_ORDONNANCEMENT', 'SIGNER_ACTE'));

INSERT INTO mandat_permissions (mandat_id, permission)
SELECT m.id, 'SIGNER_ACTE'
FROM mandat m
WHERE m.role_systeme IN ('ORDONNATEUR', 'MINISTRE')
  AND NOT EXISTS (SELECT 1 FROM mandat_permissions mp
                  WHERE mp.mandat_id = m.id AND mp.permission = 'SIGNER_ACTE');

INSERT INTO mandat_permissions (mandat_id, permission)
SELECT m.id, 'SIGNER_ACTE'
FROM mandat m
JOIN users u ON u.id = m.user_id
WHERE lower(u.email) = 'admin@gbe.cm'
  AND m.role_systeme = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM mandat_permissions mp
                  WHERE mp.mandat_id = m.id AND mp.permission = 'SIGNER_ACTE');
