package gov.cmr.minfi.db.gbe.app.depense.besoin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Les besoins supprimés logiquement sont exclus d'office (@SQLRestriction sur l'entité). */
public interface ExpressionBesoinRepository extends JpaRepository<ExpressionBesoin, String> {

    /** Filtres facultatifs : un paramètre null n'est pas appliqué. */
    @Query("""
            SELECT b FROM ExpressionBesoin b JOIN FETCH b.tache
            WHERE (:exerciceId IS NULL OR b.exercice.id = :exerciceId)
              AND (:sectionId IS NULL OR b.section.id = :sectionId)
              AND (:programmeId IS NULL OR b.programme.id = :programmeId)
              AND (:statut IS NULL OR b.statut = :statut)
            ORDER BY b.reference DESC
            """)
    List<ExpressionBesoin> rechercher(@Param("exerciceId") String exerciceId,
                                      @Param("sectionId") String sectionId,
                                      @Param("programmeId") String programmeId,
                                      @Param("statut") StatutExpression statut);
}
