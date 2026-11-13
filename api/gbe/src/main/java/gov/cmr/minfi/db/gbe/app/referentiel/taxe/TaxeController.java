package gov.cmr.minfi.db.gbe.app.referentiel.taxe;

import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.dto.TaxeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/referentiel/taxes")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.REF_TAXES)
public class TaxeController {

    private final TaxeService taxeService;

    @Operation(
        summary = "Lister les taux de taxes",
        description = "Retourne les taxes appliquées par le serveur aux offres et aux actes : TVA (ajoutée au HT pour le TTC) " +
            "et IR (retenu à la source, déduit du net à payer)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des taxes")
    })
    @GetMapping
    public ResponseEntity<List<TaxeResponse>> getTaxes() {
        return ResponseEntity.ok(taxeService.getTaxes());
    }
}
