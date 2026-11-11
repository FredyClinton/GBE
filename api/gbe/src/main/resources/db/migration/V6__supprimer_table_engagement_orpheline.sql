-- ============================================================
-- GBE — V6 : suppression de la table orpheline ENGAGEMENT
--
-- Créée par ddl-auto=update à partir du brouillon budget/depense/Engagement,
-- abandonné depuis. Aucune entité ne la mappe et rien ne la référence.
-- Le futur module de dépenses la recréera par sa propre migration.
-- ============================================================
DROP TABLE IF EXISTS engagement;
