package gov.cmr.minfi.db.gbe.app.depense.soumission;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.SoumissionRequest;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.SoumissionResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Soumissions du fournisseur connecté (UC03) : connexion obligatoire (Q9), permission SOUMISSIONNER.
 * L'examen des offres par l'ordonnateur relève du lot 2.
 */
@RestController
@RequestMapping("/api/v1/soumissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SOUMISSIONNER')")
@Tag(name = "Soumissions", description = "Dépôt et suivi des offres par le fournisseur (UC03)")
public class SoumissionController {

    private final SoumissionService soumissionService;

    /** Dépôt : renvoie le récépissé (numéro, horodatage, empreinte). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SoumissionResponse deposer(@Valid @RequestBody SoumissionRequest request) {
        return soumissionService.deposer(request);
    }

    /** Modification jusqu'à la date limite du besoin ; nouveau sceau. */
    @PutMapping("/{soumissionId}")
    @ResponseStatus(HttpStatus.OK)
    public SoumissionResponse modifier(@PathVariable String soumissionId,
                                       @Valid @RequestBody SoumissionRequest request) {
        return soumissionService.modifier(soumissionId, request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SoumissionResponse> getMesSoumissions(@ParameterObject Pageable pageable) {
        return soumissionService.getMesSoumissions(pageable);
    }

    @GetMapping("/{soumissionId}")
    @ResponseStatus(HttpStatus.OK)
    public SoumissionResponse getMaSoumission(@PathVariable String soumissionId) {
        return soumissionService.getMaSoumission(soumissionId);
    }
}
