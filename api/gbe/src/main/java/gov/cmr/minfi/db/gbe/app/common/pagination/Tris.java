package gov.cmr.minfi.db.gbe.app.common.pagination;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/** Tri d'une page restreint à une liste fermée de propriétés (pas de tri sur un chemin arbitraire). */
public final class Tris {

    private Tris() {
    }

    /** Vérifie le tri demandé ; applique {@code parDefaut} si aucun tri n'est demandé. */
    public static Pageable verifier(Pageable pageable, Set<String> autorises, Sort parDefaut) {
        for (Sort.Order order : pageable.getSort()) {
            if (!autorises.contains(order.getProperty())) {
                throw new BusinessException(ErrorCode.TRI_INVALIDE, order.getProperty(), autorises);
            }
        }
        return pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), parDefaut);
    }
}
