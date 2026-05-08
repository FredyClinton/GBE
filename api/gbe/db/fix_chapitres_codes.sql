-- ============================================================
-- SGDAI - Correction des codes chapitres anomalies (longueur 9 et 11)
-- Source : db/chapitres_codes_anomalies.csv
-- Date   : 2026-04-15
-- Usage  :
--   BD locale   : psql "postgresql://username:password@localhost:5434/sgdai_db" -f db/fix_chapitres_codes.sql
--   BD distante : psql "postgresql://sgdai_db_user:UU6YSShFGvH9FAZUvRX7LlAaCjJpywld@dpg-d7788jidbo4c73eblaeg-a.frankfurt-postgres.render.com/sgdai_db" -f db/fix_chapitres_codes.sql
--   Serveur     : psql "postgresql://USER:PASSWORD@HOST:PORT/DB" -f fix_chapitres_codes.sql
-- ============================================================

BEGIN;

UPDATE chapitres SET code = '0747121D01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE SOMALOMO'));
UPDATE chapitres SET code = '0743119201' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('PREFECTURE DE MBALMAYO'));
UPDATE chapitres SET code = '0743132A01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('PREFECTURE DE YAGOUA'));
UPDATE chapitres SET code = '0743140001' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('PREFECTURE DE NKONGSAMBA'));
UPDATE chapitres SET code = '0747119001' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS PREFECTURE DE AKOEMAN'));
UPDATE chapitres SET code = '0747119101' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE DZENG'));
UPDATE chapitres SET code = '0747119201' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS PREFECTURE DE MBALMAYO'));
UPDATE chapitres SET code = '0747119301' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS PREFECTURE DE MENGUEME'));
UPDATE chapitres SET code = '0747119401' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS PREFECTURE DE NKOL-METET'));
UPDATE chapitres SET code = '0747119501' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS PREFECTURE DE NGOMEDZAP'));
UPDATE chapitres SET code = '0747121901' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE MESSAMENA'));
UPDATE chapitres SET code = '0747121A01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE MESSOK'));
UPDATE chapitres SET code = '0747121B01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE NGUELEMENDOUKA'));
UPDATE chapitres SET code = '0747121C01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE NGOYLA'));
UPDATE chapitres SET code = '0747131901' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE ZINA'));
UPDATE chapitres SET code = '0747132901' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE WINA'));
UPDATE chapitres SET code = '0747132A01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE YAGOUA'));
UPDATE chapitres SET code = '0747140901' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE NKONGSAMBA I'));
UPDATE chapitres SET code = '0747140A01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE NKONGSAMBA II'));
UPDATE chapitres SET code = '0747140B01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE NKONGSAMBA III'));
UPDATE chapitres SET code = '0747140C01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE NLONAKO'));
UPDATE chapitres SET code = '0747142901' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE NYANON'));
UPDATE chapitres SET code = '0747142A01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE POUMA'));
UPDATE chapitres SET code = '0747150901' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE PITOA'));
UPDATE chapitres SET code = '0747150A01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE TCHEBOA'));
UPDATE chapitres SET code = '0747150B01' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SOUS-PREFECTURE DE TOUROUA'));
UPDATE chapitres SET code = '0708119201' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('SIEGE DU TRIBUNAL DE PREMIERE INSTANCE DE MBALMAYO'));
UPDATE chapitres SET code = '0708119202' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('PARQUET DU TRIBUNAL DE PREMIERE INSTANCE MBALMAYO'));
UPDATE chapitres SET code = '0785119201' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('PRISON PRINCIPALE DE MBALMAYO'));
UPDATE chapitres SET code = '0785121901' WHERE TRIM(UPPER(libelle)) = TRIM(UPPER('PRISON SECONDAIRE DE MESSAMENA'));

-- Cas non matchés au 1er passage : libellés avec tirets/espaces/accents/sauts de ligne différents
-- Correspondance via le code anomalique actuel (non ambigu)
UPDATE chapitres SET code = '0747140B01' WHERE code = '00747142C01'; -- SOUS PREFECTURE DE NKONGSAMBA III
UPDATE chapitres SET code = '0747140C01' WHERE code = '00747142D01'; -- SOUS PREFECTURE DE NLONAKO
UPDATE chapitres SET code = '0747142901' WHERE code = '00747144A01'; -- SOUS PREFECTURE DE NYANON
UPDATE chapitres SET code = '0747142A01' WHERE code = '00747144B01'; -- SOUS PREFECTURE DE POUMA
UPDATE chapitres SET code = '0708119201' WHERE code = '0084311A001'; -- SIEGE DU TRIBUNAL DE MBALMAYO
UPDATE chapitres SET code = '0708119202' WHERE code = '0084311A002'; -- PARQUET DU TRIBUNAL DE MBALMAYO

COMMIT;

-- Vérification
SELECT COUNT(*) as anomalies_restantes FROM chapitres WHERE LENGTH(code) <> 10;
SELECT LENGTH(code) as longueur, code, libelle FROM chapitres WHERE LENGTH(code) <> 10 ORDER BY longueur, code;