package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, String> {
    boolean existsByCodeSectionAndExerciceId(String codeSection, String exerciceId);

    List<Section> findByExerciceId(String exerciceId);
    
}
