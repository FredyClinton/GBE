package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.FournisseurResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Espace du fournisseur connecté. */
@RestController
@RequestMapping("/api/v1/fournisseurs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_FOURNISSEUR')")
@Tag(name = OpenApiTags.ESPACE_FOURNISSEUR)
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @Operation(
        summary = "Consulter son profil fournisseur",
        description = "Retourne la fiche du fournisseur rattaché au compte connecté, avec ses RIB à choisir lors d'une soumission."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profil du fournisseur"),
        @ApiResponse(responseCode = "403", description = "Le compte connecté n'a pas le rôle ROLE_FOURNISSEUR"),
        @ApiResponse(responseCode = "404", description = "Aucun fournisseur rattaché au compte")
    })
    @GetMapping("/moi")
    @ResponseStatus(HttpStatus.OK)
    public FournisseurResponse getMonProfil() {
        return fournisseurService.getMonProfil();
    }
}
