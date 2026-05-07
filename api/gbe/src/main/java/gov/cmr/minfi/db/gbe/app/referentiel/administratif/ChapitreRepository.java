package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChapitreRepository extends JpaRepository<Chapitre, String> {
	/** Recherche par code complet 8 caractères (colonne SQL: code) */
	Optional<Chapitre> findByCodeComplet(String codeComplet);

	/** Vérifie l'unicité du code complet avant création */
	boolean existsByCodeComplet(String codeComplet);

	/** Chapitres d'une section */
	List<Chapitre> findBySectionId(String sectionId);

	/** Chapitres d'un exercice */
	List<Chapitre> findByExerciceId(String exerciceId);

	/** Chapitres d'une section pour un exercice donné */
	List<Chapitre> findBySectionIdAndExerciceId(String sectionId, String exerciceId);

	/**
	 * TODO : Requête CTE anti-cycle à restaurer quand chapitreParent
	 *        sera ajouté dans l'entité (colonne chapitre_parent_id en base).
	 * Pour l'instant retourne uniquement l'id du chapitre lui-même.
	 */
	@Query(value = """
            SELECT id FROM chapitres WHERE id = :chapitreId
            """, nativeQuery = true)
	List<String> findAllAncestorIds(@Param("chapitreId") String chapitreId);
}
