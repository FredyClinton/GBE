package gov.cmr.minfi.db.gbe.app.depense.besoin.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.common.pagination.Tris;
import gov.cmr.minfi.db.gbe.app.depense.besoin.BesoinOuvertService;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoinMapper;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoinRepository;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.BesoinOuvertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BesoinOuvertServiceImpl implements BesoinOuvertService {

    static final Set<String> TRIS_AUTORISES = Set.of("dateClotureSoumission", "datePublication", "reference");

    private final ExpressionBesoinRepository besoinRepository;
    private final ExpressionBesoinMapper besoinMapper;
    private final Clock clock;

    @Override
    public PageResponse<BesoinOuvertResponse> rechercher(String motCle, String natureEconomiqueId,
                                                         LocalDateTime dateLimiteAvant, Pageable pageable) {
        // Par défaut, les échéances les plus proches d'abord
        final Pageable page = Tris.verifier(pageable, TRIS_AUTORISES, Sort.by("dateClotureSoumission"));
        final String motif = motCle == null || motCle.isBlank()
                ? null
                : "%" + motCle.trim().toLowerCase(Locale.ROOT) + "%";
        return PageResponse.of(
                besoinRepository.rechercherOuverts(LocalDateTime.now(clock), natureEconomiqueId, dateLimiteAvant,
                        motif, page),
                besoinMapper::toBesoinOuvertResume);
    }

    @Override
    public BesoinOuvertResponse getBesoinOuvert(String besoinId) {
        return besoinRepository.findById(besoinId)
                .filter(b -> b.isOuverteAuxSoumissions(LocalDateTime.now(clock)))
                .map(besoinMapper::toBesoinOuvert)
                // Même réponse qu'un id inexistant : ne pas révéler l'existence d'un brouillon
                .orElseThrow(() -> new BusinessException(ErrorCode.BESOIN_NOT_FOUND, besoinId));
    }
}
