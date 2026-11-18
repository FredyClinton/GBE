package gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeuilFormeActeRepository extends JpaRepository<SeuilFormeActe, String> {

    List<SeuilFormeActe> findAllByOrderByRangAsc();
}
