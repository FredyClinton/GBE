package gov.cmr.minfi.db.gbe.app.referentiel.economique;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NatureEconomiqueRepository extends JpaRepository<NatureEconomique, String> {
	Optional<NatureEconomique> findByCode(String code);

	List<NatureEconomique> findByExerciceId(String exerciceId);

	List<NatureEconomique> findByCodeTitre(String codeTitre);

	List<NatureEconomique> findByCodeArticle(String codeArticle);

	boolean existsByCodeAndExerciceId(String code, String exerciceId);
}
