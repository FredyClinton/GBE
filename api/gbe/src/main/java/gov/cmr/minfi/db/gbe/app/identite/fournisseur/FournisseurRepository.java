package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FournisseurRepository extends JpaRepository<Fournisseur, String> {

    @EntityGraph(attributePaths = "user")
    List<Fournisseur> findAllByOrderByRaisonSocialeAsc();

    Optional<Fournisseur> findByUserId(String userId);

    boolean existsByUserId(String userId);

    boolean existsByNiu(String niu);

    boolean existsByNiuAndIdNot(String niu, String id);
}
