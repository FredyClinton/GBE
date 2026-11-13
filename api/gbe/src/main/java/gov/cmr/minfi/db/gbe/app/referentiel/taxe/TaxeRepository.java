package gov.cmr.minfi.db.gbe.app.referentiel.taxe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxeRepository extends JpaRepository<Taxe, String> {

    Optional<Taxe> findByCode(CodeTaxe code);
}
