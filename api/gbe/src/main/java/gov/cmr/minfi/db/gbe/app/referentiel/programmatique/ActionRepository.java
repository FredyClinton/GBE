package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActionRepository extends JpaRepository<Action, String> {
	List<Action> findByProgrammeId(String programmeId);

	List<Action> findBySectionId(String sectionId);

	List<Action> findByExerciceId(String exerciceId);

	Optional<Action> findByCodeAction(String codeAction);

	boolean existsByCodeActionAndProgrammeId(String codeAction, String programmeId);
}
