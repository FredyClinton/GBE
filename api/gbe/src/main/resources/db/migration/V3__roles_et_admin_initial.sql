-- ============================================================
-- GBE — V3 : rôles applicatifs et super-administrateur initial
-- Remplace l'ancien DataInitializer.
--
-- Migration idempotente : elle s'exécute aussi sur les bases existantes
-- (baseline-version = 2) et n'insère que ce qui manque.
--
-- Compte créé : admin@gbe.cm / Admin@1234 (first_login = true : changement
-- de mot de passe imposé à la première connexion ; secret MFA généré au login).
-- ============================================================

-- 1. Un rôle par valeur de RoleSysteme (nom = 'ROLE_' || RoleSysteme.name())
INSERT INTO roles (id, created_by, created_date, name)
SELECT gen_random_uuid()::text, 'SYSTEM', CURRENT_DATE, r.name
FROM (VALUES ('ROLE_ADMIN'), ('ROLE_ORDONNATEUR'), ('ROLE_MINISTRE'), ('ROLE_CONTROLEUR_FINANCIER'),
             ('ROLE_COMPTABLE'), ('ROLE_GESTIONNAIRE'), ('ROLE_AGENT')) AS r(name)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE roles.name = r.name);

-- 2. Section technique, utilisateur admin et mandat de section (toutes permissions)
DO $$
DECLARE
    v_section_id text;
    v_user_id    text;
    v_mandat_id  text := gen_random_uuid()::text;
BEGIN
    IF EXISTS (SELECT 1 FROM users WHERE lower(email) = 'admin@gbe.cm') THEN
        RETURN;
    END IF;

    SELECT id INTO v_section_id FROM sections WHERE code = '00' AND sigle = 'SYS' LIMIT 1;
    IF v_section_id IS NULL THEN
        v_section_id := gen_random_uuid()::text;
        INSERT INTO sections (id, created_by, created_date, code, sigle, libelle, libelle_en)
        VALUES (v_section_id, 'SYSTEM', CURRENT_DATE, '00', 'SYS', 'Administration Système', 'System Administration');
    END IF;

    v_user_id := gen_random_uuid()::text;
    INSERT INTO users (id, created_date, first_name, last_name, email, phone_number, password, role_id,
                       is_enabled, is_account_locked, credentials_expired, is_email_verified, phone_verified,
                       first_login, mfa_enabled)
    VALUES (v_user_id, CURRENT_DATE, 'Super', 'Admin', 'admin@gbe.cm', '+237600000000',
            '$2a$10$7TxRVM4bA3S5DwNwmAG6Qe6oQ205pgCf77XcAecqL4jik3/Ptg4U6',   -- bcrypt(Admin@1234)
            (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'),
            true, false, false, false, false, true, false);

    INSERT INTO mandat (id, created_by, created_date, actif, start_mandat, role_systeme, section_id, user_id)
    VALUES (v_mandat_id, 'SYSTEM', CURRENT_DATE, true, CURRENT_DATE, 'ADMIN', v_section_id, v_user_id);

    INSERT INTO mandat_permissions (mandat_id, permission)
    SELECT v_mandat_id, p
    FROM unnest(ARRAY['ENGAGE_DEPENSE', 'REVISER_AE', 'REVISER_CP', 'REJETER_DEPENSE', 'VISA_CFI', 'REJETER_CFI',
                      'LIQUIDER_DEPENSE', 'PAYER_DEPENSE', 'INSCRIRE_CREDIT', 'MANAGE_USERS',
                      'MANAGE_AFFECTATIONS', 'CONSULTER']) AS p;
END $$;
