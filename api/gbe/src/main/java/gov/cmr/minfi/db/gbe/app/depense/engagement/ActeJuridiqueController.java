package gov.cmr.minfi.db.gbe.app.depense.engagement;

import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.ActeJuridiqueResponse;
import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.CreationActeRequest;
import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.ModificationActeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Actes juridiques (UC04) : préparation du projet par l'ordonnateur et envoi au CF. */
@RestController
@RequestMapping("/api/v1/actes")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.DEPENSE_ACTES)
public class ActeJuridiqueController {

    private final ActeJuridiqueService acteService;

    @Operation(
        summary = "Préparer le projet d'acte d'un besoin attribué",
        description = "Crée le projet d'acte (statut PROJET) à partir d'un besoin ATTRIBUEE et de son offre retenue. " +
            "Seule la forme bon de commande administratif (BCA) est disponible. " +
            "Le numéro suit le format BCA-A<code exercice><code section><numéro d'ordre sur 5 chiffres>. " +
            "L'acte reprend l'objet du besoin, le fournisseur, le RIB, les lignes et les montants de l'offre retenue " +
            "(HT, TVA, IR, TTC, net à payer), avec le TTC en lettres. " +
            "La date d'émission est la date du jour ; le délai de livraison en mois est proposé à partir du délai de l'offre " +
            "(jours ÷ 30, arrondi au supérieur) s'il n'est pas fourni. Un seul acte par besoin."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Projet d'acte créé"),
        @ApiResponse(responseCode = "400", description = "Besoin non attribué (BESOIN_NON_ATTRIBUE), forme non disponible (FORME_ACTE_NON_DISPONIBLE) ou délai invalide"),
        @ApiResponse(responseCode = "403", description = "Permission ENGAGE_DEPENSE manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Besoin ou offre retenue introuvable"),
        @ApiResponse(responseCode = "409", description = "Un acte existe déjà pour ce besoin (ACTE_DEJA_CREE)")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ENGAGE_DEPENSE')")
    public ResponseEntity<ActeJuridiqueResponse> creer(@Valid @RequestBody CreationActeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(acteService.creer(request));
    }

    @Operation(
        summary = "Lister les actes juridiques du périmètre",
        description = "Retourne les actes des besoins visibles dans le périmètre des mandats de l'utilisateur, sans les lignes. " +
            "Filtres facultatifs : exercice et statut. " +
            "Tri possible sur numero (par défaut, décroissant), dateEmission, montantTTC, statut."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page d'actes"),
        @ApiResponse(responseCode = "400", description = "Propriété de tri non autorisée (TRI_INVALIDE)")
    })
    @GetMapping
    public ResponseEntity<PageResponse<ActeJuridiqueResponse>> rechercher(
            @Parameter(description = "Filtre facultatif : identifiant de l'exercice") @RequestParam(required = false) String exerciceId,
            @Parameter(description = "Filtre facultatif : statut de l'acte") @RequestParam(required = false) StatutActe statut,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(acteService.rechercher(exerciceId, statut, pageable));
    }

    @Operation(
        summary = "Consulter un acte juridique",
        description = "Retourne le contenu complet de l'acte, tel qu'il figure sur le BCA : structure émettrice, imputation " +
            "(programme, action, activité, tâche, nature économique), fournisseur et RIB, lignes, montants et montant en lettres."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Acte trouvé"),
        @ApiResponse(responseCode = "403", description = "Hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Acte introuvable")
    })
    @GetMapping("/{acteId}")
    public ResponseEntity<ActeJuridiqueResponse> getActe(
            @Parameter(description = "Identifiant de l'acte juridique", required = true) @PathVariable String acteId) {
        return ResponseEntity.ok(acteService.getActe(acteId));
    }

    @Operation(
        summary = "Modifier un projet d'acte",
        description = "Modifie l'objet et le délai de livraison (en mois) d'un acte au statut PROJET. " +
            "Les montants, le fournisseur, le RIB et les lignes viennent de l'offre retenue et ne se modifient pas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Projet d'acte modifié"),
        @ApiResponse(responseCode = "400", description = "L'acte n'est plus un projet (ACTE_NON_MODIFIABLE) ou données invalides"),
        @ApiResponse(responseCode = "403", description = "Permission ENGAGE_DEPENSE manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Acte introuvable")
    })
    @PutMapping("/{acteId}")
    @PreAuthorize("hasAuthority('ENGAGE_DEPENSE')")
    public ResponseEntity<ActeJuridiqueResponse> modifier(
            @Parameter(description = "Identifiant de l'acte juridique", required = true) @PathVariable String acteId,
            @Valid @RequestBody ModificationActeRequest request) {
        return ResponseEntity.ok(acteService.modifier(acteId, request));
    }

    @Operation(
        summary = "Soumettre le projet d'acte au contrôle du CF",
        description = "Fait passer l'acte de PROJET à SOUMIS_CONTROLE : il n'est plus modifiable par l'ordonnateur. " +
            "La ligne de crédit est déduite de l'imputation du besoin (exercice, section, programme, action de la tâche, " +
            "chapitre, nature économique) ; l'envoi est refusé si elle n'existe pas. " +
            "La disponibilité des AE est vérifiée par le CF lors de son contrôle."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Acte soumis au contrôle du CF"),
        @ApiResponse(responseCode = "400", description = "Transition de statut invalide ou aucune ligne de crédit pour l'imputation (CREDIT_IMPUTATION_INTROUVABLE)"),
        @ApiResponse(responseCode = "403", description = "Permission ENGAGE_DEPENSE manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Acte introuvable")
    })
    @PatchMapping("/{acteId}/soumettre")
    @PreAuthorize("hasAuthority('ENGAGE_DEPENSE')")
    public ResponseEntity<ActeJuridiqueResponse> soumettreAuControle(
            @Parameter(description = "Identifiant de l'acte juridique", required = true) @PathVariable String acteId) {
        return ResponseEntity.ok(acteService.soumettreAuControle(acteId));
    }
}
