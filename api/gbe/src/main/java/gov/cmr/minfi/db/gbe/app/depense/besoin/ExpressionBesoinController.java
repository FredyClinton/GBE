package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.AnnulationBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

/**
 * Expressions de besoin côté administration (UC01). Le périmètre des mandats est contrôlé dans le service.
 * La consultation par les fournisseurs (besoins ouverts, UC02) a ses propres routes.
 */
@RestController
@RequestMapping("/api/v1/besoins")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.DEPENSE_BESOINS)
public class ExpressionBesoinController {

    private final ExpressionBesoinService besoinService;

    @Operation(
        summary = "Créer une expression de besoin",
        description = "Permet à l'ordonnateur d'exprimer un besoin, enregistré au statut BROUILLON avec une référence EB-<année>-00001. " +
            "Le besoin s'inscrit dans une tâche, dont sont déduits l'exercice, la section et le programme. " +
            "Il porte un chapitre de la même section, une nature économique de l'exercice, une date limite des soumissions et des lignes (une matière par ligne, une quantité, des spécifications libres). " +
            "Aucun prix n'est saisi."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Besoin créé au statut BROUILLON"),
        @ApiResponse(responseCode = "400", description = "Données invalides, matière en double, date limite passée, chapitre hors de la section ou nature économique hors de l'exercice"),
        @ApiResponse(responseCode = "403", description = "Permission EXPRIMER_BESOIN manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Tâche, chapitre, nature économique ou matière introuvable")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse creer(@Valid @RequestBody ExpressionBesoinRequest request) {
        return besoinService.creer(request);
    }

    /**
     * Besoins du périmètre de l'utilisateur, paginés : {@code ?page=0&size=20&sort=datePublication,desc}.
     * Tri possible sur reference (défaut, décroissant), dateExpression, datePublication, dateClotureSoumission, statut.
     */
    @Operation(
        summary = "Lister les expressions de besoin du périmètre",
        description = "Retourne les besoins visibles dans le périmètre des mandats de l'utilisateur, filtrés en base. " +
            "Filtres facultatifs : exercice, section, programme, statut. " +
            "Tri possible sur reference (par défaut, décroissant), dateExpression, datePublication, dateClotureSoumission, statut."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page de besoins"),
        @ApiResponse(responseCode = "400", description = "Propriété de tri non autorisée (TRI_INVALIDE)")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ExpressionBesoinResponse> rechercher(
            @Parameter(description = "Filtre facultatif : identifiant de l'exercice") @RequestParam(required = false) String exerciceId,
            @Parameter(description = "Filtre facultatif : identifiant de la section") @RequestParam(required = false) String sectionId,
            @Parameter(description = "Filtre facultatif : identifiant du programme") @RequestParam(required = false) String programmeId,
            @Parameter(description = "Filtre facultatif : statut du besoin") @RequestParam(required = false) StatutExpression statut,
            @ParameterObject Pageable pageable) {
        return besoinService.rechercher(exerciceId, sectionId, programmeId, statut, pageable);
    }

    @Operation(
        summary = "Consulter une expression de besoin",
        description = "Retourne le détail d'un besoin et de ses lignes."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Besoin trouvé"),
        @ApiResponse(responseCode = "403", description = "hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Besoin introuvable")
    })
    @GetMapping("/{besoinId}")
    @ResponseStatus(HttpStatus.OK)
    public ExpressionBesoinResponse getBesoin(@Parameter(description = "Identifiant de l'expression de besoin", required = true) @PathVariable String besoinId) {
        return besoinService.getBesoin(besoinId);
    }

    @Operation(
        summary = "Modifier une expression de besoin",
        description = "Remplace le contenu du besoin (mêmes champs qu'à la création). " +
            "Possible pour un BROUILLON, ou pour un besoin PUBLIEE tant qu'aucune soumission n'est déposée et que la date limite n'est pas atteinte ; un besoin publié doit alors rester complet."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Besoin modifié"),
        @ApiResponse(responseCode = "400", description = "Besoin non modifiable (BESOIN_NON_MODIFIABLE) ou données invalides"),
        @ApiResponse(responseCode = "403", description = "Permission EXPRIMER_BESOIN manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Besoin, tâche, chapitre, nature économique ou matière introuvable")
    })
    @PutMapping("/{besoinId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse modifier(@Parameter(description = "Identifiant de l'expression de besoin", required = true) @PathVariable String besoinId,
                                             @Valid @RequestBody ExpressionBesoinRequest request) {
        return besoinService.modifier(besoinId, request);
    }

    @Operation(
        summary = "Publier une expression de besoin",
        description = "Fait passer le besoin de BROUILLON à PUBLIEE. " +
            "Il devient visible sans compte et ouvert aux soumissions jusqu'à sa date limite. " +
            "Le besoin doit avoir au moins une ligne et une date limite future."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Besoin publié"),
        @ApiResponse(responseCode = "400", description = "Transition de statut invalide, besoin sans ligne ou date limite passée"),
        @ApiResponse(responseCode = "403", description = "Permission EXPRIMER_BESOIN manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Besoin introuvable")
    })
    @PatchMapping("/{besoinId}/publier")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse publier(@Parameter(description = "Identifiant de l'expression de besoin", required = true) @PathVariable String besoinId) {
        return besoinService.publier(besoinId);
    }

    @Operation(
        summary = "Clôturer les soumissions d'un besoin",
        description = "Fait passer le besoin de PUBLIEE à EN_EVALUATION, une fois la date limite passée. " +
            "Les dépôts sont déjà refusés automatiquement après la date limite ; cette action ouvre l'évaluation."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Soumissions clôturées"),
        @ApiResponse(responseCode = "400", description = "Transition de statut invalide ou date limite non atteinte (BESOIN_CLOTURE_PREMATUREE)"),
        @ApiResponse(responseCode = "403", description = "Permission EXPRIMER_BESOIN manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Besoin introuvable")
    })
    @PatchMapping("/{besoinId}/cloturer")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse cloturerSoumissions(@Parameter(description = "Identifiant de l'expression de besoin", required = true) @PathVariable String besoinId) {
        return besoinService.cloturerSoumissions(besoinId);
    }

    @Operation(
        summary = "Annuler une expression de besoin publiée",
        description = "Fait passer le besoin de PUBLIEE à ANNULEE, avec un motif obligatoire. " +
            "Ce statut est terminal."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Besoin annulé"),
        @ApiResponse(responseCode = "400", description = "Transition de statut invalide ou motif manquant"),
        @ApiResponse(responseCode = "403", description = "Permission EXPRIMER_BESOIN manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Besoin introuvable")
    })
    @PatchMapping("/{besoinId}/annuler")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public ExpressionBesoinResponse annuler(@Parameter(description = "Identifiant de l'expression de besoin", required = true) @PathVariable String besoinId,
                                            @Valid @RequestBody AnnulationBesoinRequest request) {
        return besoinService.annuler(besoinId, request);
    }

    /** Suppression logique d'un brouillon. */
    @Operation(
        summary = "Supprimer un brouillon",
        description = "Supprime logiquement un besoin au statut BROUILLON : il disparaît des listes et des consultations."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Brouillon supprimé"),
        @ApiResponse(responseCode = "400", description = "Le besoin n'est pas un brouillon (TRANSITION_STATUT_INVALIDE)"),
        @ApiResponse(responseCode = "403", description = "Permission EXPRIMER_BESOIN manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Besoin introuvable")
    })
    @DeleteMapping("/{besoinId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('EXPRIMER_BESOIN')")
    public void supprimer(@Parameter(description = "Identifiant de l'expression de besoin", required = true) @PathVariable String besoinId) {
        besoinService.supprimer(besoinId);
    }
}
