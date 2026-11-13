package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.BesoinOuvertResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/** Consultation publique des besoins ouverts aux soumissions (UC02), sans compte. */
public interface BesoinOuvertService {

    /**
     * @param motCle          cherché dans l'objet et dans la référence ou le libellé des matières (facultatif)
     * @param natureEconomiqueId famille de prestations (facultatif)
     * @param dateLimiteAvant ne garder que les besoins dont la date limite est au plus tard celle-ci (facultatif)
     */
    PageResponse<BesoinOuvertResponse> rechercher(String motCle, String natureEconomiqueId,
                                                  LocalDateTime dateLimiteAvant, Pageable pageable);

    /** Détail d'un besoin ouvert ; un besoin non publié, échu ou clos est introuvable. */
    BesoinOuvertResponse getBesoinOuvert(String besoinId);
}
