package gov.cmr.minfi.db.gbe.app.admin;

import gov.cmr.minfi.db.gbe.app.admin.dto.CreateUserRequest;
import gov.cmr.minfi.db.gbe.app.admin.dto.RoleResponse;
import gov.cmr.minfi.db.gbe.app.admin.dto.UserSummaryResponse;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
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

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = OpenApiTags.ADMIN_UTILISATEURS)
public class AdminController {

    private final AdminService adminService;
    private final AdminMapper adminMapper;

    @Operation(
        summary = "Créer un compte agent",
        description = "Crée le compte d'un agent interne. " +
            "Les rôles externes (fournisseur) sont refusés : leur compte se crée avec le fournisseur."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Compte créé"),
        @ApiResponse(responseCode = "400", description = "Données invalides, e-mail déjà utilisé ou rôle externe"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @PostMapping("/users")
    public ResponseEntity<UserSummaryResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @Operation(
        summary = "Lister les comptes",
        description = "Retourne tous les comptes utilisateurs."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des comptes"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @Operation(
        summary = "Consulter un compte",
        description = "Retourne un compte utilisateur."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Compte trouvé"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserSummaryResponse> getUser(@Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId) {
        return ResponseEntity.ok(adminService.getUser(userId));
    }

    @Operation(
        summary = "Activer un compte",
        description = "Active un compte utilisateur."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte activé"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<Void> activateUserAccount(@Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId) {
        adminService.activeUserAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Désactiver un compte",
        description = "Désactive un compte utilisateur."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte désactivé"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUserAccount(@Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId) {
        adminService.deactivateUserAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Supprimer un compte",
        description = "Supprime un compte utilisateur ; refusé pour le compte d'un fournisseur."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte supprimé"),
        @ApiResponse(responseCode = "400", description = "Compte rattaché à un fournisseur (FOURNISSEUR_LIE_AU_COMPTE)"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUserAccount(@Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable String userId) {
        adminService.deleteUserAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Lister les rôles",
        description = "Retourne les rôles système et leurs permissions par défaut."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des rôles"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @GetMapping("/users/roles")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(Arrays.stream(RoleSysteme.values()).map(adminMapper::toRoleResponse).toList());
    }
}
