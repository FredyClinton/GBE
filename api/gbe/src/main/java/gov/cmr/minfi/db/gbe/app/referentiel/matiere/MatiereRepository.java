package gov.cmr.minfi.db.gbe.app.referentiel.matiere;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatiereRepository extends JpaRepository<Matiere, String> {

    List<Matiere> findAllByOrderByReferenceAsc();

    boolean existsByReference(String reference);

    boolean existsByReferenceAndIdNot(String reference, String id);
}
