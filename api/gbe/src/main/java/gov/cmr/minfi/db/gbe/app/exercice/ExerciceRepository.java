package gov.cmr.minfi.db.gbe.app.exercice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExerciceRepository extends JpaRepository<Exercice, String> {

    boolean existsByAnnee(Integer annee);

    Optional<Exercice> findByActifTrue();
}