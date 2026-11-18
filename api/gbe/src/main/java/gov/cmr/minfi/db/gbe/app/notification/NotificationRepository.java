package gov.cmr.minfi.db.gbe.app.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByDestinataireId(String destinataireId, Pageable pageable);

    Page<Notification> findByDestinataireIdAndDateLectureIsNull(String destinataireId, Pageable pageable);

    long countByDestinataireIdAndDateLectureIsNull(String destinataireId);

    Optional<Notification> findByIdAndDestinataireId(String id, String destinataireId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.dateLecture = :maintenant "
            + "WHERE n.destinataire.id = :destinataireId AND n.dateLecture IS NULL")
    int marquerToutesLues(@Param("destinataireId") String destinataireId, @Param("maintenant") LocalDateTime maintenant);
}
