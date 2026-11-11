package gov.cmr.minfi.db.gbe.app.budget.credit;

import gov.cmr.minfi.db.gbe.app.budget.credit.dto.CreateCreditRequest;
import gov.cmr.minfi.db.gbe.app.budget.credit.dto.CreditBudgetaireResponse;
import gov.cmr.minfi.db.gbe.app.budget.credit.dto.UpdateCreditBudgetaireRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/credits")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.BUDGET_CREDITS)
public class CreditBudgetaireController {

    private final CreditBudgetaireService creditBudgetaireService;


    // Création
    @Operation(
        summary = "Inscrire un crédit budgétaire",
        description = "Crée une ligne de crédit DISPONIBLE sur une imputation unique : exercice, section, programme, action, chapitre et nature économique."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Crédit inscrit"),
        @ApiResponse(responseCode = "400", description = "Imputation déjà utilisée, action hors du programme, nature hors de l'exercice ou montants invalides"),
        @ApiResponse(responseCode = "403", description = "Permission INSCRIRE_CREDIT manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Élément de l'imputation introuvable")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INSCRIRE_CREDIT')")
    public CreditBudgetaireResponse createCredit(
            @Valid @RequestBody CreateCreditRequest request
    ) {
        return creditBudgetaireService.createCredit(request);
    }


    // Consultation
    @Operation(
        summary = "Consulter un crédit budgétaire",
        description = "Retourne une ligne de crédit et ses montants AE/CP."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Crédit trouvé"),
        @ApiResponse(responseCode = "403", description = "hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Crédit introuvable")
    })
    @GetMapping("/{creditId}")
    @ResponseStatus(HttpStatus.OK)
    public CreditBudgetaireResponse getCredit(
            @Parameter(description = "Identifiant du crédit budgétaire", required = true) @PathVariable String creditId
    ) {
        return creditBudgetaireService.getCredit(creditId);
    }

    @Operation(
        summary = "Lister les crédits d'un exercice",
        description = "Retourne les crédits de l'exercice visibles dans le périmètre des mandats."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des crédits")
    })
    @GetMapping("/exercice/{exerciceId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CreditBudgetaireResponse> getCreditsByExercice(
            @Parameter(description = "Identifiant de l'exercice", required = true) @PathVariable String exerciceId
    ) {
        return creditBudgetaireService.getCreditsByExercice(exerciceId);
    }

    @Operation(
        summary = "Lister les crédits d'une section pour un exercice",
        description = "Retourne les crédits de la section, limités aux programmes autorisés par les mandats."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des crédits"),
        @ApiResponse(responseCode = "403", description = "hors du périmètre des mandats de l'utilisateur")
    })
    @GetMapping("/section/{sectionId}/exercice/{exerciceId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CreditBudgetaireResponse> getCreditsBySection(
            @Parameter(description = "Identifiant de la section", required = true) @PathVariable String sectionId,
            @Parameter(description = "Identifiant de l'exercice", required = true) @PathVariable String exerciceId
    ) {
        return creditBudgetaireService.getCreditsBySection(sectionId, exerciceId);
    }

    @Operation(
        summary = "Lister les crédits d'un programme",
        description = "Retourne les crédits du programme visibles dans le périmètre des mandats."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des crédits")
    })
    @GetMapping("/programme/{programmeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CreditBudgetaireResponse> getCreditsByProgramme(
            @Parameter(description = "Identifiant du programme", required = true) @PathVariable String programmeId
    ) {
        return creditBudgetaireService.getCreditsByProgramme(programmeId);
    }


    // Révision des montants
    @Operation(
        summary = "Réviser les autorisations d'engagement",
        description = "Modifie le montant des AE d'une ligne de crédit."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "AE révisées"),
        @ApiResponse(responseCode = "400", description = "Montant invalide"),
        @ApiResponse(responseCode = "403", description = "Permission REVISER_AE manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Crédit introuvable")
    })
    @PatchMapping("/{creditId}/ae")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REVISER_AE')")
    public void updateAE(
            @Parameter(description = "Identifiant du crédit budgétaire", required = true) @PathVariable String creditId,
            @Valid @RequestBody UpdateCreditBudgetaireRequest request
    ) {
        creditBudgetaireService.updateAE(creditId, request);
    }

    @Operation(
        summary = "Réviser les crédits de paiement",
        description = "Modifie le montant des CP d'une ligne de crédit."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "CP révisés"),
        @ApiResponse(responseCode = "400", description = "Montant invalide"),
        @ApiResponse(responseCode = "403", description = "Permission REVISER_CP manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Crédit introuvable")
    })
    @PatchMapping("/{creditId}/cp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REVISER_CP')")
    public void updateCP(
            @Parameter(description = "Identifiant du crédit budgétaire", required = true) @PathVariable String creditId,
            @Valid @RequestBody UpdateCreditBudgetaireRequest request
    ) {
        creditBudgetaireService.updateCP(creditId, request);
    }


    // Transitions de statut
    @Operation(
        summary = "Suspendre un crédit",
        description = "Fait passer le crédit de DISPONIBLE à SUSPENDU : la ligne ne peut plus être consommée."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Crédit suspendu"),
        @ApiResponse(responseCode = "400", description = "Transition de statut invalide"),
        @ApiResponse(responseCode = "403", description = "Permission REVISER_AE manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Crédit introuvable")
    })
    @PatchMapping("/{creditId}/suspendre")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REVISER_AE')")
    public void suspendreCredit(
            @Parameter(description = "Identifiant du crédit budgétaire", required = true) @PathVariable String creditId
    ) {
        creditBudgetaireService.suspendreCredit(creditId);
    }

    @Operation(
        summary = "Débloquer un crédit",
        description = "Fait passer le crédit de SUSPENDU à DISPONIBLE."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Crédit débloqué"),
        @ApiResponse(responseCode = "400", description = "Transition de statut invalide"),
        @ApiResponse(responseCode = "403", description = "Permission REVISER_AE manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Crédit introuvable")
    })
    @PatchMapping("/{creditId}/debloquer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REVISER_AE')")
    public void debloquerCredit(
            @Parameter(description = "Identifiant du crédit budgétaire", required = true) @PathVariable String creditId
    ) {
        creditBudgetaireService.debloquerCredit(creditId);
    }

    @Operation(
        summary = "Cantonner un crédit",
        description = "Fait passer le crédit de DISPONIBLE à CANTONNE : la ligne est mise en réserve et ne peut plus être consommée."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Crédit cantonné"),
        @ApiResponse(responseCode = "400", description = "Transition de statut invalide"),
        @ApiResponse(responseCode = "403", description = "Permission REVISER_AE manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Crédit introuvable")
    })
    @PatchMapping("/{creditId}/cantonner")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REVISER_AE')")
    public void cantionnerCredit(
            @Parameter(description = "Identifiant du crédit budgétaire", required = true) @PathVariable String creditId
    ) {
        creditBudgetaireService.cantionnerCredit(creditId);
    }

    @Operation(
        summary = "Décantonner un crédit",
        description = "Fait passer le crédit de CANTONNE à DISPONIBLE."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Crédit décantonné"),
        @ApiResponse(responseCode = "400", description = "Transition de statut invalide"),
        @ApiResponse(responseCode = "403", description = "Permission REVISER_AE manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Crédit introuvable")
    })
    @PatchMapping("/{creditId}/decantionner")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REVISER_AE')")
    public void decantionnerCredit(
            @Parameter(description = "Identifiant du crédit budgétaire", required = true) @PathVariable String creditId
    ) {
        creditBudgetaireService.decantionnerCredit(creditId);
    }

    @Operation(
        summary = "Annuler un crédit",
        description = "Fait passer le crédit de DISPONIBLE, CANTONNE ou SUSPENDU à ANNULE, statut terminal. " +
            "L'annulation est refusée tant que des AE ou des CP sont consommés."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Crédit annulé"),
        @ApiResponse(responseCode = "400", description = "Transition de statut invalide ou crédits consommés (CREDIT_ANNULATION_CREDITS_CONSOMMES)"),
        @ApiResponse(responseCode = "403", description = "Permission REVISER_AE manquante ou hors du périmètre des mandats de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Crédit introuvable")
    })
    @PatchMapping("/{creditId}/annuler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('REVISER_AE')")
    public void annulerCredit(
            @Parameter(description = "Identifiant du crédit budgétaire", required = true) @PathVariable String creditId
    ) {
        creditBudgetaireService.annulerCredit(creditId);
    }
}
