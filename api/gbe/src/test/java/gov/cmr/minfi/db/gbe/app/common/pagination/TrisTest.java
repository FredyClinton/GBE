package gov.cmr.minfi.db.gbe.app.common.pagination;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrisTest {

    private static final Set<String> AUTORISES = Set.of("reference", "datePublication");
    private static final Sort DEFAUT = Sort.by(Sort.Direction.DESC, "reference");

    @Test
    void sansTri_appliqueLeTriParDefaut() {
        final Pageable p = Tris.verifier(PageRequest.of(2, 10), AUTORISES, DEFAUT);
        assertThat(p.getPageNumber()).isEqualTo(2);
        assertThat(p.getPageSize()).isEqualTo(10);
        assertThat(p.getSort()).isEqualTo(DEFAUT);
    }

    @Test
    void triAutorise_conserve() {
        final Pageable demande = PageRequest.of(0, 20, Sort.by("datePublication"));
        assertThat(Tris.verifier(demande, AUTORISES, DEFAUT)).isSameAs(demande);
    }

    @Test
    void triNonAutorise_refuse() {
        assertThatThrownBy(() -> Tris.verifier(PageRequest.of(0, 20, Sort.by("section.libelleFr")), AUTORISES, DEFAUT))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TRI_INVALIDE);
    }
}
