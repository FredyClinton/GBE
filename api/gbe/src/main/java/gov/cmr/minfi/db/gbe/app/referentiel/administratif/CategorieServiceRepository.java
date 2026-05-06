package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorieServiceRepository
        extends JpaRepository<CategorieService, String> {

    boolean existsByCode(String code);
}