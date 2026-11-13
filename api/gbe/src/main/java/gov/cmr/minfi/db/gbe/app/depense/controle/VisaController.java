package gov.cmr.minfi.db.gbe.app.depense.controle;

import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import gov.cmr.minfi.db.gbe.app.depense.controle.dto.DecisionVisaRequest;
import gov.cmr.minfi.db.gbe.app.depense.controle.dto.VisaResponse;
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

/** Contrôle du CF sur l'acte juridique (UC06 « include » UC07, UC08). */
@RestController
@RequestMapping("/api/v1/actes/{acteId}/visas")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.DEPENSE_CONTROLE_CF)
public class VisaController {

    private final ControleCfService controleCfService;

    @Operation(
        summary = "Rendre la décision du CF sur un acte soumis",
        description = "Une seule décision, enregistrée d'un bloc dans un visa (type ENGAGEMENT_JURIDIQUE). " +
            "FAVORABLE : le quitus sur la nature économique est porté par le visa, les AE de la ligne de crédit sont réservées " +
            "pour le TTC de l'acte et l'acte passe à VISE_CF ; refusé si la ligne est bloquée ou si les AE disponibles " +
            "sont insuffisantes, le CF rejette alors l'acte. " +
            "DEFAVORABLE_REJET : motif obligatoire, l'acte revient en PROJET pour correction et rien n'est réservé. " +
            "SUSPENDU_RESERVES : motif obligatoire, l'acte reste SOUMIS_CONTROLE en attente de compléments."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Visa enregistré"),
        @ApiResponse(responseCode = "400", description = "Acte non soumis au contrôle (TRANSITION_STATUT_INVALIDE), motif manquant (VISA_MOTIF_OBLIGATOIRE), " +
            "ligne de crédit bloquée (CREDIT_NON_CONSOMMABLE) ou AE insuffisantes (MONTANT_AE_INSUFFISANT)"),
        @ApiResponse(responseCode = "403", description = "Permission VISA_CFI (et, pour un visa favorable, RESERVER_CREDIT et QUITUS_NATURE) manquante ou hors du périmètre des mandats"),
        @ApiResponse(responseCode = "404", description = "Acte introuvable")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('VISA_CFI')")
    public ResponseEntity<VisaResponse> decider(
            @Parameter(description = "Identifiant de l'acte juridique", required = true) @PathVariable String acteId,
            @Valid @RequestBody DecisionVisaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(controleCfService.decider(acteId, request));
    }

    @Operation(
        summary = "Historique des visas d'un acte",
        description = "Retourne les décisions du CF sur l'acte, de la plus ancienne à la plus récente : rejets, réserves et visa favorable."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des visas"),
        @ApiResponse(responseCode = "403", description = "Hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Acte introuvable")
    })
    @GetMapping
    public ResponseEntity<List<VisaResponse>> getVisas(
            @Parameter(description = "Identifiant de l'acte juridique", required = true) @PathVariable String acteId) {
        return ResponseEntity.ok(controleCfService.getVisas(acteId));
    }
}
