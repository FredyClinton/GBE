-- ============================================================
-- GBE — V23 : signature, notification et annulation de l'acte (lot 2d du module Dépense, UC05)
--
-- Signature tracée (Q6) : signataire et date. Onglet 2 : ordonnateur.signer() puis notifierFournisseur(),
-- enchaînés dans la même opération (Q20) : VISE_CF → SIGNE_ORDONNATEUR → NOTIFIE_PRESTATAIRE.
-- Annulation d'un acte visé, signé ou notifié (Q2) : motif et date ; la réservation passe ANNULEE et les AE
-- sont libérées sur la ligne de crédit.
-- ============================================================

ALTER TABLE actes_juridiques
    ADD COLUMN signataire_id     character varying(255),
    ADD COLUMN date_signature    timestamp(0) without time zone,
    ADD COLUMN date_notification timestamp(0) without time zone,
    ADD COLUMN motif_annulation  text,
    ADD COLUMN date_annulation   timestamp(0) without time zone,
    ADD CONSTRAINT fk_acte_signataire FOREIGN KEY (signataire_id) REFERENCES users (id);

-- Espace du fournisseur : ses actes notifiés
CREATE INDEX IF NOT EXISTS idx_acte_date_notification ON actes_juridiques (date_notification)
    WHERE date_notification IS NOT NULL;
