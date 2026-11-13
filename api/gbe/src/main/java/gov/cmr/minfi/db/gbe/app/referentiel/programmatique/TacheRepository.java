package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, String> {

	@EntityGraph(attributePaths = "activite")
	List<Tache> findByActiviteIdOrderByCode(String activiteId);

	List<Tache> findByActiviteId(String activiteId);

	@EntityGraph(attributePaths = "activite")
	List<Tache> findByProgrammeIdOrderByCode(String programmeId);

	@EntityGraph(attributePaths = "activite")
	List<Tache> findByProgrammeIdAndExerciceIdOrderByCode(String programmeId, String exerciceId);

	boolean existsByActiviteId(String activiteId);

	boolean existsByActiviteIdAndCode(String activiteId, String code);

	boolean existsByActiviteIdAndCodeAndIdNot(String activiteId, String code, String id);
}
