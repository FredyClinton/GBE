package gov.cmr.minfi.db.gbe.app.depense.soumission;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.SoumissionRequest;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.SoumissionResponse;
import org.springframework.data.domain.Pageable;

/** Soumissions du fournisseur connecté (UC03) ; il ne voit que les siennes. */
public interface SoumissionService {

    SoumissionResponse deposer(SoumissionRequest request);

    SoumissionResponse modifier(String soumissionId, SoumissionRequest request);

    SoumissionResponse getMaSoumission(String soumissionId);

    PageResponse<SoumissionResponse> getMesSoumissions(Pageable pageable);
}
