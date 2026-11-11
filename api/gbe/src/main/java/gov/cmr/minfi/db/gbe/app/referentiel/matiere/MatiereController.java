package gov.cmr.minfi.db.gbe.app.referentiel.matiere;

import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Consultation du catalogue des matières (tout utilisateur authentifié). */
@RestController
@RequestMapping("/api/v1/referentiel/matieres")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.REF_MATIERES)
public class MatiereController {

    private final MatiereService matiereService;

    @Operation(
        summary = "Lister les matières",
        description = "Retourne le catalogue complet des matières (référence, libellé)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des matières")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MatiereResponse> getAllMatieres() {
        return matiereService.getAllMatieres();
    }

    @Operation(
        summary = "Consulter une matière",
        description = "Retourne une matière du catalogue."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matière trouvée"),
        @ApiResponse(responseCode = "404", description = "Matière introuvable")
    })
    @GetMapping("/{matiereId}")
    @ResponseStatus(HttpStatus.OK)
    public MatiereResponse getMatiere(@Parameter(description = "Identifiant de la matière", required = true) @PathVariable String matiereId) {
        return matiereService.getMatiere(matiereId);
    }
}
