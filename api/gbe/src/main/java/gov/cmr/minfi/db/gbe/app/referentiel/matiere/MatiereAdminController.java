package gov.cmr.minfi.db.gbe.app.referentiel.matiere;

import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Gestion du catalogue des matières. Même permission que la gestion des actions. */
@RestController
@RequestMapping("/api/v1/admin/matieres")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = OpenApiTags.REF_MATIERES)
public class MatiereAdminController {

    private final MatiereService matiereService;

    @Operation(
        summary = "Créer une matière",
        description = "Ajoute une matière au catalogue : référence unique et libellé. " +
            "Une matière n'est pas liée à une nature économique."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Matière créée"),
        @ApiResponse(responseCode = "400", description = "Référence déjà utilisée ou données invalides"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @PostMapping
    public ResponseEntity<MatiereResponse> createMatiere(@Valid @RequestBody MatiereRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matiereService.createMatiere(request));
    }

    @Operation(
        summary = "Modifier une matière",
        description = "Met à jour la référence et le libellé d'une matière."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matière modifiée"),
        @ApiResponse(responseCode = "400", description = "Référence déjà utilisée ou données invalides"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Matière introuvable")
    })
    @PutMapping("/{matiereId}")
    public ResponseEntity<MatiereResponse> updateMatiere(@Parameter(description = "Identifiant de la matière", required = true) @PathVariable String matiereId, @Valid @RequestBody MatiereRequest request) {
        return ResponseEntity.ok(matiereService.updateMatiere(matiereId, request));
    }

    @Operation(
        summary = "Supprimer une matière",
        description = "Retire une matière du catalogue."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Matière supprimée"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Matière introuvable")
    })
    @DeleteMapping("/{matiereId}")
    public ResponseEntity<Void> deleteMatiere(@Parameter(description = "Identifiant de la matière", required = true) @PathVariable String matiereId) {
        matiereService.deleteMatiere(matiereId);
        return ResponseEntity.noContent().build();
    }
}
