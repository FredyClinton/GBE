package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChapitreRepository extends JpaRepository<Chapitre, String> {
    List<Chapitre> findBySectionId(String sectionId);

    List<Chapitre> findByChapitreParentId(String chapitreParentId);

    // Récupère tous les ancêtres d'un chapitre pour détecter les cycles
    // Utilisé avant d'affecter un chapitreParent
    @Query(value = """
            WITH RECURSIVE ancetres AS (
                SELECT id, chapitre_tutelle_id
                FROM chapitre
                WHERE id = :chapitreId
                UNION ALL
                SELECT c.id, c.chapitre_tutelle_id
                FROM chapitre c
                INNER JOIN ancetres a ON c.id = a.chapitre_tutelle_id
            )
            SELECT id FROM ancetres
            """, nativeQuery = true)
    List<String> findAllAncestorIds(@Param("chapitreId") String chapitreId);

}