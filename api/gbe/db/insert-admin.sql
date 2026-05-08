-- ========================
-- Insertion utilisateur Admin
-- Email    : admin@sgdai.cm
-- Password : Admin@1234
-- Rôle     : ROLE_ADMIN
-- ========================

-- S'assurer que le rôle ROLE_ADMIN existe
INSERT INTO roles (id, created_by, created_date, last_modified_date, last_midified_by, name)
VALUES ('8b9a77ee-6b96-4bc4-8033-4b98b4893225', 'SYSTEM', '2026-04-23', NULL, NULL, 'ROLE_ADMIN')
    ON CONFLICT (id) DO NOTHING;

-- Insérer l'utilisateur admin
INSERT INTO users (
    id, created_date, credentials_expired, date_of_birth,
    email, is_email_verified, is_enabled, is_credentials_expired,
    first_name, last_modified_date, last_name, is_account_locked,
    mfa_enabled, first_login, password, phone_number, phone_verified, secret
) VALUES (
             'e4fe8f18-c80f-4940-a836-fbee940dbb93',
             '2026-04-23',
             false, NULL,
             'admin@sgdai.cm',
             true, true, false,
             'Admin', NULL, 'Admin',
             false, false, true,
             '$2b$12$MROSoL/ee3.G2twXILuDQ.PCqivk4Q6SGIJ252sZAPQ/SGXvd5xqC',
             '655000000',
             true, 'QDWSM3OYBPGTEVSPB5FKVDM3CSNCWHVK'
         ) ON CONFLICT (email) DO NOTHING;

-- Assigner ROLE_ADMIN
INSERT INTO users_roles (users_id, roles_id)
VALUES (
           'e4fe8f18-c80f-4940-a836-fbee940dbb93',
           '8b9a77ee-6b96-4bc4-8033-4b98b4893225'
       ) ON CONFLICT DO NOTHING;
