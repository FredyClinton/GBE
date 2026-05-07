package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgrammeRepository extends JpaRepository<Programme, String> {
	List<Programme> findBySectionId(String sectionId);

	default List<Programme> findBySectionIdAndActifTrue(String sectionId) {
		return findBySectionId(sectionId);
	}

	/** Programmes d'un exercice */
	List<Programme> findByExerciceId(String exerciceId);

	/** Programmes d'une section pour un exercice donné */
	List<Programme> findBySectionIdAndExerciceId(String sectionId, String exerciceId);

	Optional<Programme> findByCode(String code);

	boolean existsByCodeAndExerciceId(String code, String exerciceId);
}
