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
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void updateProfileInfo(
            @RequestBody
            @Valid
            ProfileUpdateRequest request,
            final Authentication principal
            ){
        this.userServices.updateProfileInfo(request, getUserId(principal));
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
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            final Authentication principal
            ){
        this.userServices.changePassword(request, getUserId(principal));
    }

    @Operation(
        summary = "Désactiver son compte",
        description = "Désactive le compte de l'utilisateur connecté."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte désactivé")
    })
    @PatchMapping("/me/deactivate")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void deactivateAccount( final Authentication principal){
        this.userServices.deactivateAccount(getUserId(principal));
    }

    @Operation(
        summary = "Réactiver son compte",
        description = "Réactive le compte de l'utilisateur connecté."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte réactivé")
    })
    @PatchMapping("/me/reactivate")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void reactivateAccount( final Authentication principal){
        this.userServices.reactivateAccount(getUserId(principal));
    }

    @Operation(
        summary = "Supprimer son compte",
        description = "Supprime le compte de l'utilisateur connecté."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte supprimé")
    })
    @DeleteMapping("/me")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void deleteAccount( final Authentication principal){
        this.userServices.deleteAccount(getUserId(principal));
    }




    private String getUserId(final Authentication principal) {
        return  ((User) Objects.requireNonNull(principal.getPrincipal())).getId();
    }
}
