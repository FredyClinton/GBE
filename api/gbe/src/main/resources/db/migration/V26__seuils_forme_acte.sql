-- ============================================================
-- GBE — V26 : seuils des formes d'acte (Q3, décision 2026-11-18)
--
-- Échelle BCA < LC < Marché, sur le montant TTC de l'acte (FCFA) ; plafond exclusif :
--   - bon de commande administratif : TTC < 5 000 000 ;
--   - lettre-commande               : TTC < 50 000 000 ;
--   - marché public                 : sans plafond.
-- La convention de bail est hors échelle (imposée par la nature), donc absente de la table.
-- Le système propose la forme minimale et refuse une forme inférieure. Table vide : choix libre.
-- La contrainte CHECK liste TypeActe : ajouter un type demande une migration.
-- ============================================================

CREATE TABLE IF NOT EXISTS seuils_forme_acte (
    id                 character varying(255) NOT NULL,
    created_by         character varying(255) NOT NULL,
    created_date       date                   NOT NULL,
    last_modified_date timestamp(6) without time zone,
    last_midified_by   character varying(255),
    type_acte          character varying(30)  NOT NULL,
    rang               integer                NOT NULL,
    plafond_ttc        numeric(20, 2),
    CONSTRAINT seuils_forme_acte_pkey PRIMARY KEY (id),
    CONSTRAINT uk_seuil_type_acte UNIQUE (type_acte),
    CONSTRAINT uk_seuil_rang UNIQUE (rang),
    CONSTRAINT seuils_forme_acte_type_check CHECK (type_acte IN (
        'BON_DE_COMMANDE', 'LETTRE_COMMANDE', 'MARCHE_PUBLIC', 'CONVENTION_BAIL')),
    CONSTRAINT ck_seuil_plafond CHECK (plafond_ttc IS NULL OR plafond_ttc > 0)
);

INSERT INTO seuils_forme_acte (id, created_by, created_date, type_acte, rang, plafond_ttc)
SELECT gen_random_uuid()::text, 'SYSTEM', CURRENT_DATE, s.type_acte, s.rang, s.plafond
FROM (VALUES ('BON_DE_COMMANDE', 1, 5000000.00),
             ('LETTRE_COMMANDE', 2, 50000000.00),
             ('MARCHE_PUBLIC', 3, NULL::numeric)) AS s(type_acte, rang, plafond)
WHERE NOT EXISTS (SELECT 1 FROM seuils_forme_acte x WHERE x.type_acte = s.type_acte);
