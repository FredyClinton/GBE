package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.CreateFournisseurRequest;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.FournisseurResponse;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.RibRequest;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.UpdateFournisseurRequest;
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

/**
 * Gestion des fournisseurs par l'administrateur (décision Q5 : pas d'auto-inscription).
 * Activation / désactivation du compte : routes /api/v1/admin/users/{userId}/activate|deactivate.
 */
@RestController
@RequestMapping("/api/v1/admin/fournisseurs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = OpenApiTags.ADMIN_FOURNISSEURS)
public class FournisseurAdminController {

    private final FournisseurService fournisseurService;

    @Operation(
        summary = "Créer un fournisseur et son compte",
        description = "Crée le fournisseur (raison sociale, NIU, RCCM, adresse, téléphone de l'entreprise) et le compte ROLE_FOURNISSEUR de son représentant, sans agent ni mandat. " +
            "Les RIB sont facultatifs à la création. Un RIB compte 23 caractères (banque 5 chiffres, guichet 5 chiffres, compte 11 caractères, clé 2 chiffres) ; espaces et tirets sont retirés."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Fournisseur créé"),
        @ApiResponse(responseCode = "400", description = "NIU, e-mail ou téléphone déjà utilisé, RIB invalide ou en double"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @PostMapping
    public ResponseEntity<FournisseurResponse> createFournisseur(@Valid @RequestBody CreateFournisseurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fournisseurService.createFournisseur(request));
    }

    @Operation(
        summary = "Lister les fournisseurs",
        description = "Retourne tous les fournisseurs avec leurs RIB."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des fournisseurs"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @GetMapping
    public ResponseEntity<List<FournisseurResponse>> getAllFournisseurs() {
        return ResponseEntity.ok(fournisseurService.getAllFournisseurs());
    }

    @Operation(
        summary = "Consulter un fournisseur",
        description = "Retourne le fournisseur, son représentant et ses RIB."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fournisseur trouvé"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable")
    })
    @GetMapping("/{fournisseurId}")
    public ResponseEntity<FournisseurResponse> getFournisseur(@Parameter(description = "Identifiant du fournisseur", required = true) @PathVariable String fournisseurId) {
        return ResponseEntity.ok(fournisseurService.getFournisseur(fournisseurId));
    }

    @Operation(
        summary = "Modifier un fournisseur",
        description = "Met à jour les informations de l'entreprise et de son représentant. " +
            "Les RIB se gèrent par les routes /ribs."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fournisseur modifié"),
        @ApiResponse(responseCode = "400", description = "NIU ou téléphone déjà utilisé"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable")
    })
    @PutMapping("/{fournisseurId}")
    public ResponseEntity<FournisseurResponse> updateFournisseur(
            @Parameter(description = "Identifiant du fournisseur", required = true) @PathVariable String fournisseurId,
            @Valid @RequestBody UpdateFournisseurRequest request) {
        return ResponseEntity.ok(fournisseurService.updateFournisseur(fournisseurId, request));
    }

    @Operation(
        summary = "Ajouter un RIB à un fournisseur",
        description = "Un RIB compte 23 caractères (banque 5 chiffres, guichet 5 chiffres, compte 11 caractères, clé 2 chiffres) ; espaces et tirets sont retirés. " +
            "Un fournisseur peut avoir plusieurs RIB et choisit celui de chaque offre."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "RIB ajouté, fournisseur retourné"),
        @ApiResponse(responseCode = "400", description = "RIB invalide ou déjà enregistré pour ce fournisseur"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable")
    })
    @PostMapping("/{fournisseurId}/ribs")
    public ResponseEntity<FournisseurResponse> ajouterRib(@Parameter(description = "Identifiant du fournisseur", required = true) @PathVariable String fournisseurId, @Valid @RequestBody RibRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fournisseurService.ajouterRib(fournisseurId, request));
    }

    @Operation(
        summary = "Retirer un RIB d'un fournisseur",
        description = "Retire un RIB du fournisseur, sauf s'il est utilisé par une soumission."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "RIB retiré, fournisseur retourné"),
        @ApiResponse(responseCode = "400", description = "RIB utilisé par une soumission (RIB_UTILISE)"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Fournisseur ou RIB introuvable")
    })
    @DeleteMapping("/{fournisseurId}/ribs/{ribId}")
    public ResponseEntity<FournisseurResponse> retirerRib(@Parameter(description = "Identifiant du fournisseur", required = true) @PathVariable String fournisseurId, @Parameter(description = "Identifiant du RIB (coordonnée bancaire)", required = true) @PathVariable String ribId) {
        return ResponseEntity.ok(fournisseurService.retirerRib(fournisseurId, ribId));
    }
}
