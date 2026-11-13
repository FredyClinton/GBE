package gov.cmr.minfi.db.gbe.app.referentiel.taxe;

import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.dto.TauxTaxeRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.dto.TaxeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/taxes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = OpenApiTags.REF_TAXES)
public class TaxeAdminController {

    private final TaxeService taxeService;

    @Operation(
        summary = "Modifier le taux d'une taxe",
        description = "Change le taux d'une taxe (TVA ou IR). " +
            "Le nouveau taux s'applique aux offres déposées ou modifiées ensuite ; les montants déjà enregistrés ne changent pas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Taux modifié"),
        @ApiResponse(responseCode = "400", description = "Taux absent, négatif ou supérieur ou égal à 100"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Taxe non paramétrée")
    })
    @PutMapping("/{code}")
    public ResponseEntity<TaxeResponse> changerTaux(
            @Parameter(description = "Code de la taxe (TVA ou IR)", required = true) @PathVariable CodeTaxe code,
            @Valid @RequestBody TauxTaxeRequest request) {
        return ResponseEntity.ok(taxeService.changerTaux(code, request));
    }
}
