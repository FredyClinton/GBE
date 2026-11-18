-- Bénéficiaire du besoin (Q27, décision 2026-11-18) : les bénéficiaires sont des chapitres.
-- Association « au profit de (bénéficiaire) » de ExpressionBesoin vers Chapitre (drawio, onglets 1 et 4),
-- distincte de la structure émettrice (chapitre_id). Imprimé sur le BCA (« Bénéficiaire : 11000020 - MINFI »).
-- Les besoins existants prennent leur structure émettrice comme bénéficiaire, valeur par défaut de l'API.

ALTER TABLE expressions_besoin ADD COLUMN beneficiaire_id character varying(255);

UPDATE expressions_besoin SET beneficiaire_id = chapitre_id WHERE beneficiaire_id IS NULL;

ALTER TABLE expressions_besoin ALTER COLUMN beneficiaire_id SET NOT NULL;

ALTER TABLE expressions_besoin
    ADD CONSTRAINT fk_expression_besoin_beneficiaire FOREIGN KEY (beneficiaire_id) REFERENCES chapitres (id);
