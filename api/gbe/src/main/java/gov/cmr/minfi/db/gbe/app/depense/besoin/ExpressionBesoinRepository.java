package gov.cmr.minfi.db.gbe.app.depense.besoin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;

/** Les besoins supprimés logiquement sont exclus d'office (@SQLRestriction sur l'entité). */
public interface ExpressionBesoinRepository extends JpaRepository<ExpressionBesoin, String> {

    /**
     * Besoins du périmètre (sections entières ou programmes) ; filtres facultatifs (null : non appliqué).
     * Les collections ne doivent pas être vides (le service passe une valeur sentinelle).
     */
    @EntityGraph(attributePaths = "tache")
    @Query("""
            SELECT b FROM ExpressionBesoin b
            WHERE (b.section.id IN :sectionsEntieres OR b.programme.id IN :programmes)
              AND (:exerciceId IS NULL OR b.exercice.id = :exerciceId)
              AND (:sectionId IS NULL OR b.section.id = :sectionId)
              AND (:programmeId IS NULL OR b.programme.id = :programmeId)
              AND (:statut IS NULL OR b.statut = :statut)
            """)
    Page<ExpressionBesoin> rechercherDansPerimetre(@Param("sectionsEntieres") Collection<String> sectionsEntieres,
                                                   @Param("programmes") Collection<String> programmes,
                                                   @Param("exerciceId") String exerciceId,
                                                   @Param("sectionId") String sectionId,
                                                   @Param("programmeId") String programmeId,
                                                   @Param("statut") StatutExpression statut,
                                                   Pageable pageable);

    /**
     * Besoins ouverts aux soumissions (publiés, date limite non atteinte), pour la consultation publique.
     * {@code motif} : motif LIKE déjà en minuscules (« %papier% »), cherché dans l'objet et les matières.
     */
    @EntityGraph(attributePaths = {"section", "chapitre", "natureEconomique"})
    @Query("""
            SELECT b FROM ExpressionBesoin b
            WHERE b.statut = gov.cmr.minfi.db.gbe.app.depense.besoin.StatutExpression.PUBLIEE
              AND b.dateClotureSoumission > :maintenant
              AND (:natureEconomiqueId IS NULL OR b.natureEconomique.id = :natureEconomiqueId)
              AND (CAST(:dateLimiteAvant AS LocalDateTime) IS NULL OR b.dateClotureSoumission <= :dateLimiteAvant)
              AND (:motif IS NULL
                   OR LOWER(b.objet) LIKE :motif
                   OR EXISTS (SELECT 1 FROM LigneBesoin l
                              WHERE l.expressionBesoin = b
                                AND (LOWER(l.matiere.libelle) LIKE :motif OR LOWER(l.matiere.reference) LIKE :motif)))
            """)
    Page<ExpressionBesoin> rechercherOuverts(@Param("maintenant") LocalDateTime maintenant,
                                             @Param("natureEconomiqueId") String natureEconomiqueId,
                                             @Param("dateLimiteAvant") LocalDateTime dateLimiteAvant,
                                             @Param("motif") String motif,
                                             Pageable pageable);
}
