package gov.cmr.minfi.db.gbe.app.depense.soumission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoumissionRepository extends JpaRepository<Soumission, String> {

    @EntityGraph(attributePaths = {"expressionBesoin", "coordonneeBancaire"})
    Page<Soumission> findByFournisseurId(String fournisseurId, Pageable pageable);

    Optional<Soumission> findByIdAndFournisseurId(String id, String fournisseurId);

    @EntityGraph(attributePaths = {"fournisseur", "coordonneeBancaire"})
    Page<Soumission> findByExpressionBesoinId(String expressionBesoinId, Pageable pageable);

    List<Soumission> findAllByExpressionBesoinId(String expressionBesoinId);

    Optional<Soumission> findByIdAndExpressionBesoinId(String id, String expressionBesoinId);

    boolean existsByExpressionBesoinIdAndFournisseurId(String expressionBesoinId, String fournisseurId);

    boolean existsByExpressionBesoinId(String expressionBesoinId);

    boolean existsByCoordonneeBancaireId(String coordonneeBancaireId);
}
