package gov.cmr.minfi.db.gbe.app.referentiel.taxe.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.CodeTaxe;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.Taxe;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.TaxeRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.TaxeService;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.dto.TauxTaxeRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.dto.TaxeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TaxeServiceImpl implements TaxeService {

    private final TaxeRepository taxeRepository;

    @Override
    public List<TaxeResponse> getTaxes() {
        return taxeRepository.findAll(Sort.by("code")).stream().map(TaxeServiceImpl::toResponse).toList();
    }

    /** Le nouveau taux s'applique aux offres déposées ou modifiées ensuite ; les montants enregistrés ne changent pas. */
    @Override
    @Transactional
    public TaxeResponse changerTaux(CodeTaxe code, TauxTaxeRequest request) {
        final Taxe taxe = taxeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAXE_NON_PARAMETREE, code));
        final var ancien = taxe.getTaux();
        taxe.changerTaux(request.taux());
        log.info("Taux {} modifié : {} → {}", code, ancien, taxe.getTaux());
        return toResponse(taxe);
    }

    private static TaxeResponse toResponse(Taxe t) {
        return new TaxeResponse(t.getCode(), t.getLibelle(), t.getTaux());
    }
}
