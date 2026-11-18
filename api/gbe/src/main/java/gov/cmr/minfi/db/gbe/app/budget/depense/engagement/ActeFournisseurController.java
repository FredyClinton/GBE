package gov.cmr.minfi.db.gbe.app.budget.depense.engagement;

import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.dto.ActeJuridiqueResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Actes notifiés au fournisseur connecté (UC05, Q20). */
@RestController
@RequestMapping("/api/v1/fournisseurs/moi/actes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_FOURNISSEUR')")
@Tag(name = OpenApiTags.ESPACE_FOURNISSEUR)
public class ActeFournisseurController {

    private final ActeJuridiqueService acteService;

    @Operation(
        summary = "Lister ses actes notifiés",
        description = "Retourne les actes signés et notifiés au fournisseur connecté, y compris ceux annulés ensuite, " +
            "sans les lignes. Les projets et les actes en cours de contrôle n'y figurent pas. " +
            "Tri possible sur dateNotification (par défaut, décroissant), numero, montantTTC, statut."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page d'actes notifiés"),
        @ApiResponse(responseCode = "400", description = "Propriété de tri non autorisée (TRI_INVALIDE)"),
        @ApiResponse(responseCode = "403", description = "Le compte connecté n'a pas le rôle ROLE_FOURNISSEUR"),
        @ApiResponse(responseCode = "404", description = "Aucun fournisseur rattaché au compte")
    })
    @GetMapping
    public ResponseEntity<PageResponse<ActeJuridiqueResponse>> mesActes(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(acteService.mesActesFournisseur(pageable));
    }

    @Operation(
        summary = "Consulter un de ses actes notifiés",
        description = "Retourne le contenu du BCA notifié (lignes, montants, imputation, signature), sans la ligne de " +
            "crédit ni la réservation. L'acte d'un autre fournisseur, ou pas encore notifié, est introuvable."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Acte notifié"),
        @ApiResponse(responseCode = "403", description = "Le compte connecté n'a pas le rôle ROLE_FOURNISSEUR"),
        @ApiResponse(responseCode = "404", description = "Acte introuvable ou non notifié à ce fournisseur (ACTE_NOT_FOUND)")
    })
    @GetMapping("/{acteId}")
    public ResponseEntity<ActeJuridiqueResponse> monActe(
            @Parameter(description = "Identifiant de l'acte juridique", required = true) @PathVariable String acteId) {
        return ResponseEntity.ok(acteService.getActeFournisseur(acteId));
    }
}
