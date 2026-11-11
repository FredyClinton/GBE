-- ============================================================
-- GBE — V17 : attribution d'un besoin (lot 2a du module Dépense)
--
-- Onglet 2 : EN_EVALUATION → ATTRIBUEE (retenirSoumission) ou → INFRUCTUEUSE (infructueux()).
-- Décision Q13 : l'ordonnateur sélectionne seulement l'offre retenue (commission externe, Q4) ;
-- l'offre retenue passe RETENUE, les autres NON_RETENUE (Q16 : c'est ainsi que les fournisseurs
-- non retenus sont informés). Besoin infructueux ou annulé : les offres restent DEPOSEE (Q15).
--   expressions_besoin.date_decision : date de l'attribution ou du constat d'infructuosité
--   soumissions.date_decision        : date à laquelle l'offre a été retenue ou écartée
-- ============================================================

ALTER TABLE expressions_besoin ADD COLUMN IF NOT EXISTS date_decision timestamp(0) without time zone;
ALTER TABLE soumissions ADD COLUMN IF NOT EXISTS date_decision timestamp(0) without time zone;

-- Attribution globale (rapport, section 9) : une seule offre retenue par besoin
CREATE UNIQUE INDEX IF NOT EXISTS uk_soumission_retenue_par_besoin
    ON soumissions (expression_besoin_id) WHERE statut = 'RETENUE';

CREATE INDEX IF NOT EXISTS idx_soumission_besoin ON soumissions (expression_besoin_id);
