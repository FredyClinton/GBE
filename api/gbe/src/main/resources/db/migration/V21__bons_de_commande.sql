-- ============================================================
-- GBE — V21 : données propres à la forme BCA (lot 2b, refonte demandée le 2026-11-11)
--
-- Onglet 4 : ActeJuridique « applique forme contractuelle » (1 ↔ 1) une FormeActeStrategie qui porte ses propres
-- attributs ; StrategieBonDeCommande : referenceDevis, delaiLivraisonMaxMois, codeBarresSecurite
-- (onglet 1, BonDeCommandeAdministratif : delaiLivraisonMois, codeBarres, qrCodeSecurite).
-- Composition plutôt qu'héritage : la forme d'un acte peut changer (changerForme de l'onglet 4).
-- Chaque forme aura sa table (lettres_commande, marches_publics…) ; le délai quitte l'acte pour la forme.
-- Code-barres et QR code : remplis avec l'impression (lot 3, plan §7).
-- ============================================================

CREATE TABLE IF NOT EXISTS bons_de_commande (
    id                   character varying(255) NOT NULL,
    created_by           character varying(255) NOT NULL,
    created_date         date                   NOT NULL,
    last_modified_date   timestamp(6) without time zone,
    last_midified_by     character varying(255),
    delai_livraison_mois integer                NOT NULL,
    reference_devis      character varying(100),
    code_barres_securite character varying(255),
    qr_code_securite     text,
    acte_juridique_id    character varying(255) NOT NULL,
    CONSTRAINT bons_de_commande_pkey PRIMARY KEY (id),
    CONSTRAINT uk_bon_de_commande_acte UNIQUE (acte_juridique_id),
    CONSTRAINT ck_bon_de_commande_delai CHECK (delai_livraison_mois > 0),
    CONSTRAINT fk_bon_de_commande_acte FOREIGN KEY (acte_juridique_id) REFERENCES actes_juridiques (id)
);

INSERT INTO bons_de_commande (id, created_by, created_date, delai_livraison_mois, acte_juridique_id)
SELECT gen_random_uuid()::text, a.created_by, a.created_date, a.delai_livraison_mois, a.id
FROM actes_juridiques a
WHERE a.type = 'BON_DE_COMMANDE'
  AND NOT EXISTS (SELECT 1 FROM bons_de_commande b WHERE b.acte_juridique_id = a.id);

ALTER TABLE actes_juridiques DROP CONSTRAINT IF EXISTS ck_acte_delai;
ALTER TABLE actes_juridiques DROP COLUMN IF EXISTS delai_livraison_mois;
