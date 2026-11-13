package gov.cmr.minfi.db.gbe.app.identite.mandat;

import gov.cmr.minfi.db.gbe.app.identite.mandat.dto.CreateMandatRequest;
import gov.cmr.minfi.db.gbe.app.identite.mandat.dto.MandatSummary;
import gov.cmr.minfi.db.gbe.app.identite.mandat.dto.UpdateMandatRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/utilisateurs/{userId}/mandats")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_AFFECTATIONS')")
@Tag(name = OpenApiTags.ADMIN_MANDATS)
public class MandatController {

    private final MandatService mandatService;

    @Operation(
        summary = "Attribuer un mandat",
        description = "Rattache l'utilisateur à une section, et facultativement à un programme, avec un rôle ; les permissions sont initialisées depuis le rôle. " +
            "Un mandat MINISTRE sans programme couvre toute la section, les autres mandats leur seul programme. " +
            "Refusé pour un rôle externe (fournisseur)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Mandat attribué"),
        @ApiResponse(responseCode = "400", description = "Données invalides, mandat déjà existant ou rôle externe (ROLE_EXTERNE_SANS_MANDAT)"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_AFFECTATIONS manquante"),
        @ApiResponse(responseCode = "404", description = "Utilisateur, section ou programme introuvable")
    })
    @PostMapping
    public ResponseEntity<MandatSummary> createMandat(
            @Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId,
            @Valid @RequestBody CreateMandatRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mandatService.createMandat(userId, request));
    }

    @Operation(
        summary = "Lister les mandats d'un utilisateur",
        description = "Retourne les mandats de l'utilisateur."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des mandats"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_AFFECTATIONS manquante")
    })
    @GetMapping
    public ResponseEntity<List<MandatSummary>> getMandats(@Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId) {
        return ResponseEntity.ok(mandatService.getMandats(userId));
    }

    @Operation(
        summary = "Modifier un mandat",
        description = "Met à jour le rôle, le périmètre ou les permissions d'un mandat."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mandat modifié"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_AFFECTATIONS manquante"),
        @ApiResponse(responseCode = "404", description = "Mandat introuvable")
    })
    @PatchMapping("/{mandatId}")
    public ResponseEntity<MandatSummary> updateMandat(
            @Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId,
            @Parameter(description = "Identifiant du mandat", required = true) @PathVariable String mandatId,
            @Valid @RequestBody UpdateMandatRequest request
    ) {
        return ResponseEntity.ok(mandatService.updateMandat(userId, mandatId, request));
    }

    @Operation(
        summary = "Activer un mandat",
        description = "Active un mandat."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Mandat activé"),
        @ApiResponse(responseCode = "400", description = "Mandat déjà actif"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_AFFECTATIONS manquante"),
        @ApiResponse(responseCode = "404", description = "Mandat introuvable")
    })
    @PatchMapping("/{mandatId}/activer")
    public ResponseEntity<Void> activateMandat(
            @Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId,
            @Parameter(description = "Identifiant du mandat", required = true) @PathVariable String mandatId
    ) {
        mandatService.activateMandat(userId, mandatId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Désactiver un mandat",
        description = "Désactive un mandat."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Mandat désactivé"),
        @ApiResponse(responseCode = "400", description = "Mandat déjà inactif"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_AFFECTATIONS manquante"),
        @ApiResponse(responseCode = "404", description = "Mandat introuvable")
    })
    @PatchMapping("/{mandatId}/desactiver")
    public ResponseEntity<Void> deactivateMandat(
            @Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId,
            @Parameter(description = "Identifiant du mandat", required = true) @PathVariable String mandatId
    ) {
        mandatService.deactivateMandat(userId, mandatId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Supprimer un mandat",
        description = "Supprime un mandat."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Mandat supprimé"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_AFFECTATIONS manquante"),
        @ApiResponse(responseCode = "404", description = "Mandat introuvable")
    })
    @DeleteMapping("/{mandatId}")
    public ResponseEntity<Void> removeMandat(
            @Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId,
            @Parameter(description = "Identifiant du mandat", required = true) @PathVariable String mandatId
    ) {
        mandatService.removeMandat(userId, mandatId);
        return ResponseEntity.noContent().build();
    }
}
