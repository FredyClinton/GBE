-- ============================================================
-- GBE — V14 : téléphone de l'entreprise fournisseur
--
-- Attribut « telephone » de la classe Fournisseur (diagramme de classes, onglets 1 et 4).
-- Distinct du téléphone du représentant, porté par son compte (users.phone_number).
-- Exigé par l'API ; la colonne reste nullable pour ne pas bloquer d'éventuelles lignes existantes.
-- ============================================================

ALTER TABLE fournisseurs
    ADD COLUMN IF NOT EXISTS telephone character varying(30);
