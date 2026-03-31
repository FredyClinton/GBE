package gov.cmr.minfi.db.gbe.app.mandat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MandatRepository extends JpaRepository<Mandat, String> {
    List<Mandat> findByUserId(String userId);

    List<Mandat> findByUserIdAndActifTrue(String userId);

    boolean existsByUserIdAndProgrammeId(String userId, String programmeId);


    void deleteByUserIdAndProgrammeId(String userId, String programmeId);

    @Query("""
            SELECT m FROM Mandat m
            WHERE m.user.id = :userId
            AND m.actif = true
            AND (m.dateDebut IS NULL OR m.dateDebut <= CURRENT_DATE)
            AND (m.dateFin IS NULL OR m.dateFin >= CURRENT_DATE)
            """)
    List<Mandat> findMandatsValidesParUser(@Param("userId") String userId);

    // Pour le MINISTRE - mandat sans programme sur une section
    boolean existsByUserIdAndSectionAndProgrammeIsNull(String userId, String section);
}
