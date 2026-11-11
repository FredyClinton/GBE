package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiviteRepository extends JpaRepository<Activite, String> {

	@EntityGraph(attributePaths = "action")
	List<Activite> findByActionIdOrderByCode(String actionId);

	@EntityGraph(attributePaths = "action")
	List<Activite> findByProgrammeIdOrderByCode(String programmeId);

	@EntityGraph(attributePaths = "action")
	List<Activite> findByProgrammeIdAndExerciceIdOrderByCode(String programmeId, String exerciceId);

	boolean existsByActionId(String actionId);

	boolean existsByActionIdAndCode(String actionId, String code);

	boolean existsByActionIdAndCodeAndIdNot(String actionId, String code, String id);
}
