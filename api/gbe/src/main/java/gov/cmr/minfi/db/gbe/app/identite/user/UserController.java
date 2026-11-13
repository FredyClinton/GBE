package gov.cmr.minfi.db.gbe.app.identite.user;

import gov.cmr.minfi.db.gbe.app.identite.user.dto.ChangePasswordRequest;
import gov.cmr.minfi.db.gbe.app.identite.user.dto.ProfileUpdateRequest;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.Objects;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.MON_COMPTE)
public class UserController {
    private  final UserService userServices;

    @Operation(
        summary = "Modifier son profil",
        description = "Met à jour les informations personnelles de l'utilisateur connecté."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Profil modifié"),
        @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfileInfo(
            @RequestBody
            @Valid
            ProfileUpdateRequest request,
            final Authentication principal
            ){
        this.userServices.updateProfileInfo(request, getUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Changer son mot de passe",
        description = "Vérifie le mot de passe actuel puis enregistre le nouveau, qui doit être confirmé."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Mot de passe changé"),
        @ApiResponse(responseCode = "400", description = "Mot de passe actuel incorrect ou confirmation différente")
    })
    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            final Authentication principal
            ){
        this.userServices.changePassword(request, getUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Désactiver son compte",
        description = "Désactive le compte de l'utilisateur connecté."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte désactivé")
    })
    @PatchMapping("/me/deactivate")
    public ResponseEntity<Void> deactivateAccount( final Authentication principal){
        this.userServices.deactivateAccount(getUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Réactiver son compte",
        description = "Réactive le compte de l'utilisateur connecté."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte réactivé")
    })
    @PatchMapping("/me/reactivate")
    public ResponseEntity<Void> reactivateAccount( final Authentication principal){
        this.userServices.reactivateAccount(getUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Supprimer son compte",
        description = "Supprime le compte de l'utilisateur connecté."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte supprimé")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount( final Authentication principal){
        this.userServices.deleteAccount(getUserId(principal));
        return ResponseEntity.noContent().build();
    }




    private String getUserId(final Authentication principal) {
        return  ((User) Objects.requireNonNull(principal.getPrincipal())).getId();
    }
}
