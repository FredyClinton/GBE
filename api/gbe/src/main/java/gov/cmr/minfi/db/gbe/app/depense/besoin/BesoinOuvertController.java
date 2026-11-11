package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.BesoinOuvertResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Consultation publique des besoins ouverts (UC02), sans connexion (Q9) ; GET uniquement
 * (SecurityConfig.PUBLIC_GET_URLS). Soumissionner exige d'être connecté (UC03).
 */
@RestController
@RequestMapping("/api/v1/public/besoins")
@RequiredArgsConstructor
@Tag(name = "Besoins ouverts (public)", description = "Consultation des besoins ouverts aux soumissions, sans compte")
public class BesoinOuvertController {

    private final BesoinOuvertService besoinOuvertService;

    /**
     * Besoins publiés dont la date limite n'est pas atteinte, paginés : {@code ?page=0&size=20}.
     * Tri possible sur dateClotureSoumission (défaut, croissant), datePublication, reference.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<BesoinOuvertResponse> rechercher(
            @RequestParam(required = false) String motCle,
            @RequestParam(required = false) String natureEconomiqueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateLimiteAvant,
            @ParameterObject Pageable pageable) {
        return besoinOuvertService.rechercher(motCle, natureEconomiqueId, dateLimiteAvant, pageable);
    }

    @GetMapping("/{besoinId}")
    @ResponseStatus(HttpStatus.OK)
    public BesoinOuvertResponse getBesoinOuvert(@PathVariable String besoinId) {
        return besoinOuvertService.getBesoinOuvert(besoinId);
    }
}
