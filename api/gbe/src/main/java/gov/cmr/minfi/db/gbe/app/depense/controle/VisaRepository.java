package gov.cmr.minfi.db.gbe.app.depense.controle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisaRepository extends JpaRepository<Visa, String> {

    List<Visa> findByActeJuridiqueIdOrderByDateVisaAscNumeroAsc(String acteJuridiqueId);
}
