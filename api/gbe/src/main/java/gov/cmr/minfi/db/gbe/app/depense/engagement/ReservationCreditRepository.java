package gov.cmr.minfi.db.gbe.app.depense.engagement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationCreditRepository extends JpaRepository<ReservationCredit, String> {

    Optional<ReservationCredit> findByActeJuridiqueId(String acteJuridiqueId);
}
