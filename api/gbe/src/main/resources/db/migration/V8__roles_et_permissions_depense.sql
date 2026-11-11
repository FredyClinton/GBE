-- ============================================================
-- GBE — V8 : rôles et permissions du module Dépense (lot 0)
--
-- 1. Nouveaux rôles FOURNISSEUR et COMPTABLE_MATIERE (table roles + CHECK mandat).
-- 2. Nouvelles permissions (CHECK mandat_permissions).
-- 3. Mandats existants alignés sur RoleSysteme.getDefaultPermissions() :
--    - nouvelles permissions par défaut ajoutées aux ORDONNATEUR, MINISTRE, CONTROLEUR_FINANCIER ;
--    - un mandat MINISTRE sans aucune permission reçoit toutes celles du rôle (décision 2026-11-11) ;
--    - le super-admin initial (V3, « toutes permissions ») reçoit les nouvelles permissions.
-- Migration idempotente : n'insère que ce qui manque.
-- Doit rester alignée sur RoleSysteme et Permission.
-- ============================================================

-- 1. Rôles applicatifs
INSERT INTO roles (id, created_by, created_date, name)
SELECT gen_random_uuid()::text, 'SYSTEM', CURRENT_DATE, r.name
FROM (VALUES ('ROLE_FOURNISSEUR'), ('ROLE_COMPTABLE_MATIERE')) AS r(name)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE roles.name = r.name);

-- 2. Contraintes CHECK générées à l'origine par Hibernate à partir des énumérations
ALTER TABLE mandat
    DROP CONSTRAINT IF EXISTS mandat_role_systeme_check,
    ADD CONSTRAINT mandat_role_systeme_check CHECK (role_systeme IN (
        'ADMIN', 'ORDONNATEUR', 'MINISTRE', 'CONTROLEUR_FINANCIER', 'COMPTABLE', 'GESTIONNAIRE', 'AGENT',
        'FOURNISSEUR', 'COMPTABLE_MATIERE'));

ALTER TABLE mandat_permissions
    DROP CONSTRAINT IF EXISTS mandat_permissions_permission_check,
    ADD CONSTRAINT mandat_permissions_permission_check CHECK (permission IN (
        'ENGAGE_DEPENSE', 'REVISER_AE', 'REVISER_CP', 'REJETER_DEPENSE', 'VISA_CFI', 'REJETER_CFI',
        'LIQUIDER_DEPENSE', 'PAYER_DEPENSE', 'INSCRIRE_CREDIT', 'MANAGE_USERS', 'MANAGE_AFFECTATIONS', 'CONSULTER',
        'EXPRIMER_BESOIN', 'SOUMISSIONNER', 'RESERVER_CREDIT', 'QUITUS_NATURE', 'RECEPTIONNER_LIVRAISON',
        'ORDONNANCER', 'VISER_ORDONNANCEMENT'));

-- 3a. Mandat MINISTRE sans permission : permissions par défaut complètes du rôle
INSERT INTO mandat_permissions (mandat_id, permission)
SELECT m.id, p.permission
FROM mandat m
CROSS JOIN unnest(ARRAY['ENGAGE_DEPENSE', 'REVISER_AE', 'REVISER_CP', 'REJETER_DEPENSE', 'MANAGE_USERS',
                        'MANAGE_AFFECTATIONS', 'EXPRIMER_BESOIN', 'ORDONNANCER']) AS p(permission)
WHERE m.role_systeme = 'MINISTRE'
  AND NOT EXISTS (SELECT 1 FROM mandat_permissions mp WHERE mp.mandat_id = m.id);

-- 3b. Nouvelles permissions par défaut des rôles existants
INSERT INTO mandat_permissions (mandat_id, permission)
SELECT m.id, d.permission
FROM mandat m
JOIN (VALUES ('ORDONNATEUR', 'EXPRIMER_BESOIN'),
             ('ORDONNATEUR', 'ORDONNANCER'),
             ('MINISTRE', 'EXPRIMER_BESOIN'),
             ('MINISTRE', 'ORDONNANCER'),
             ('CONTROLEUR_FINANCIER', 'RESERVER_CREDIT'),
             ('CONTROLEUR_FINANCIER', 'QUITUS_NATURE'),
             ('CONTROLEUR_FINANCIER', 'VISER_ORDONNANCEMENT')) AS d(role_systeme, permission)
    ON d.role_systeme = m.role_systeme
WHERE NOT EXISTS (SELECT 1 FROM mandat_permissions mp
                  WHERE mp.mandat_id = m.id AND mp.permission = d.permission);

-- 3c. Super-admin initial : toutes les nouvelles permissions
INSERT INTO mandat_permissions (mandat_id, permission)
SELECT m.id, p.permission
FROM mandat m
JOIN users u ON u.id = m.user_id
CROSS JOIN unnest(ARRAY['EXPRIMER_BESOIN', 'SOUMISSIONNER', 'RESERVER_CREDIT', 'QUITUS_NATURE',
                        'RECEPTIONNER_LIVRAISON', 'ORDONNANCER', 'VISER_ORDONNANCEMENT']) AS p(permission)
WHERE lower(u.email) = 'admin@gbe.cm'
  AND m.role_systeme = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM mandat_permissions mp
                  WHERE mp.mandat_id = m.id AND mp.permission = p.permission);
