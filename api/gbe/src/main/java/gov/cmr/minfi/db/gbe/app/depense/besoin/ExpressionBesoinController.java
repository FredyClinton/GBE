package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.AnnulationBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

/**
 * Expressions de besoin côté administration (UC01). Le périmètre des mandats est contrôlé dans le service.
 * La consultation par les fournisseurs (besoins ouverts, UC02) a ses propres routes.
 */
@RestController
@RequestMapping("/api/v1/besoins")
@RequiredArgsConstructor
@Tag(name = "Expressions de besoin", description = "Expression, publication et suivi des besoins (UC01)")
public class ExpressionBesoinController {

    private final ExpressionBesoinService besoinService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse creer(@Valid @RequestBody ExpressionBesoinRequest request) {
        return besoinService.creer(request);
    }

    /**
     * Besoins du périmètre de l'utilisateur, paginés : {@code ?page=0&size=20&sort=datePublication,desc}.
     * Tri possible sur reference (défaut, décroissant), dateExpression, datePublication, dateClotureSoumission, statut.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ExpressionBesoinResponse> rechercher(
            @RequestParam(required = false) String exerciceId,
            @RequestParam(required = false) String sectionId,
            @RequestParam(required = false) String programmeId,
            @RequestParam(required = false) StatutExpression statut,
            @ParameterObject Pageable pageable) {
        return besoinService.rechercher(exerciceId, sectionId, programmeId, statut, pageable);
    }

    @GetMapping("/{besoinId}")
    @ResponseStatus(HttpStatus.OK)
    public ExpressionBesoinResponse getBesoin(@PathVariable String besoinId) {
        return besoinService.getBesoin(besoinId);
    }

    @PutMapping("/{besoinId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse modifier(@PathVariable String besoinId,
                                             @Valid @RequestBody ExpressionBesoinRequest request) {
        return besoinService.modifier(besoinId, request);
    }

    @PatchMapping("/{besoinId}/publier")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse publier(@PathVariable String besoinId) {
        return besoinService.publier(besoinId);
    }

    @PatchMapping("/{besoinId}/cloturer")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse cloturerSoumissions(@PathVariable String besoinId) {
        return besoinService.cloturerSoumissions(besoinId);
    }

    @PatchMapping("/{besoinId}/annuler")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse annuler(@PathVariable String besoinId,
                                            @Valid @RequestBody AnnulationBesoinRequest request) {
        return besoinService.annuler(besoinId, request);
    }

    /** Suppression logique d'un brouillon. */
    @DeleteMapping("/{besoinId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public void supprimer(@PathVariable String besoinId) {
        besoinService.supprimer(besoinId);
    }
}
