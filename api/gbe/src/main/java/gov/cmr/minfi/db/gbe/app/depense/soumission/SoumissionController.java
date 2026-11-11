package gov.cmr.minfi.db.gbe.app.depense.soumission;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.SoumissionRequest;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.SoumissionResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Soumissions du fournisseur connecté (UC03) : connexion obligatoire (Q9), permission SOUMISSIONNER.
 * L'examen des offres par l'ordonnateur relève du lot 2.
 */
@RestController
@RequestMapping("/api/v1/soumissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SOUMISSIONNER')")
@Tag(name = OpenApiTags.DEPENSE_SOUMISSIONS)
public class SoumissionController {

    private final SoumissionService soumissionService;

    /** Dépôt : renvoie le récépissé (numéro, horodatage, empreinte). */
    @Operation(
        summary = "Déposer une offre sur un besoin ouvert",
        description = "Permet au fournisseur connecté de déposer son offre sur un besoin publié dont la date limite n'est pas atteinte. " +
            "L'offre est totale : un prix unitaire HT pour chaque ligne du besoin, avec des remarques techniques facultatives, un délai proposé en jours, un montant TTC et le RIB choisi parmi ceux du fournisseur. " +
            "Le serveur calcule le total de chaque ligne (prix × quantité) et le montant HT ; le TTC doit être au moins égal au HT. " +
            "Une seule offre par fournisseur et par besoin. " +
            "Retourne le récépissé : numéro SO-<année>-00001, horodatage et empreinte SHA-256."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Offre déposée, récépissé retourné"),
        @ApiResponse(responseCode = "400", description = "Besoin non ouvert, offre incomplète, ligne inconnue ou en double, montant invalide ou TTC inférieur au HT"),
        @ApiResponse(responseCode = "403", description = "Permission SOUMISSIONNER manquante"),
        @ApiResponse(responseCode = "404", description = "Besoin ou RIB du fournisseur introuvable"),
        @ApiResponse(responseCode = "409", description = "Le fournisseur a déjà déposé une offre sur ce besoin (SOUMISSION_DEJA_DEPOSEE)")
    })
    @PostMapping
    public ResponseEntity<SoumissionResponse> deposer(@Valid @RequestBody SoumissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(soumissionService.deposer(request));
    }

    /** Modification jusqu'à la date limite du besoin ; nouveau sceau. */
    @Operation(
        summary = "Modifier son offre",
        description = "Remplace toute l'offre, RIB compris, tant que la date limite du besoin n'est pas atteinte. " +
            "L'offre est totale : un prix unitaire HT pour chaque ligne du besoin, avec des remarques techniques facultatives, un délai proposé en jours, un montant TTC et le RIB choisi parmi ceux du fournisseur. " +
            "La date de modification et l'empreinte sont recalculées. Le retrait d'une offre n'est pas possible."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Offre modifiée, nouveau récépissé retourné"),
        @ApiResponse(responseCode = "400", description = "Date limite atteinte (SOUMISSION_NON_MODIFIABLE) ou offre invalide"),
        @ApiResponse(responseCode = "403", description = "Permission SOUMISSIONNER manquante"),
        @ApiResponse(responseCode = "404", description = "Offre introuvable (y compris l'offre d'un autre fournisseur) ou RIB introuvable")
    })
    @PutMapping("/{soumissionId}")
    public ResponseEntity<SoumissionResponse> modifier(@Parameter(description = "Identifiant de la soumission", required = true) @PathVariable String soumissionId,
                                       @Valid @RequestBody SoumissionRequest request) {
        return ResponseEntity.ok(soumissionService.modifier(soumissionId, request));
    }

    @Operation(
        summary = "Lister ses offres",
        description = "Retourne les offres du fournisseur connecté, sans le détail des lignes. " +
            "Tri par défaut : dateSoumission décroissante."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page d'offres"),
        @ApiResponse(responseCode = "400", description = "Propriété de tri non autorisée (TRI_INVALIDE)"),
        @ApiResponse(responseCode = "403", description = "Permission SOUMISSIONNER manquante")
    })
    @GetMapping
    public ResponseEntity<PageResponse<SoumissionResponse>> getMesSoumissions(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(soumissionService.getMesSoumissions(pageable));
    }

    @Operation(
        summary = "Consulter une de ses offres",
        description = "Retourne le détail d'une offre du fournisseur connecté, avec ses lignes et son récépissé."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Offre trouvée"),
        @ApiResponse(responseCode = "403", description = "Permission SOUMISSIONNER manquante"),
        @ApiResponse(responseCode = "404", description = "Offre introuvable (y compris l'offre d'un autre fournisseur)")
    })
    @GetMapping("/{soumissionId}")
    public ResponseEntity<SoumissionResponse> getMaSoumission(@Parameter(description = "Identifiant de la soumission", required = true) @PathVariable String soumissionId) {
        return ResponseEntity.ok(soumissionService.getMaSoumission(soumissionId));
    }
}
