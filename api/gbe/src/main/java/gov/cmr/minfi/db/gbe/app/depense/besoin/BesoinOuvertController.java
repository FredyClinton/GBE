package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.BesoinOuvertResponse;
import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Consultation publique des besoins ouverts (UC02), sans connexion (Q9) ; GET uniquement
 * (SecurityConfig.PUBLIC_GET_URLS). Soumissionner exige d'être connecté (UC03).
 */
@RestController
@RequestMapping("/api/v1/public/besoins")
@RequiredArgsConstructor
@Tag(name = OpenApiTags.DEPENSE_BESOINS_OUVERTS)
public class BesoinOuvertController {

    private final BesoinOuvertService besoinOuvertService;

    /**
     * Besoins publiés dont la date limite n'est pas atteinte, paginés : {@code ?page=0&size=20}.
     * Tri possible sur dateClotureSoumission (défaut, croissant), datePublication, reference.
     */
    @Operation(
        summary = "Rechercher les besoins ouverts aux soumissions",
        description = "Accessible sans authentification. " +
            "Retourne les besoins publiés dont la date limite n'est pas atteinte, sans identifiant interne (tâche, programme, exercice). " +
            "Le mot-clé porte sur l'objet du besoin et sur le libellé ou la référence des matières demandées. " +
            "Tri possible sur dateClotureSoumission (par défaut, croissant), datePublication, reference."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page de besoins ouverts"),
        @ApiResponse(responseCode = "400", description = "Propriété de tri non autorisée (TRI_INVALIDE)")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<BesoinOuvertResponse> rechercher(
            @Parameter(description = "Mot-clé cherché dans l'objet du besoin et dans le libellé ou la référence des matières") @RequestParam(required = false) String motCle,
            @Parameter(description = "Filtre facultatif : identifiant de la nature économique") @RequestParam(required = false) String natureEconomiqueId,
            @Parameter(description = "Filtre facultatif : date limite des soumissions au plus tard à cette date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateLimiteAvant,
            @ParameterObject Pageable pageable) {
        return besoinOuvertService.rechercher(motCle, natureEconomiqueId, dateLimiteAvant, pageable);
    }

    @Operation(
        summary = "Consulter un besoin ouvert",
        description = "Accessible sans authentification. " +
            "Retourne le détail d'un besoin ouvert avec ses lignes (matière, quantité, spécifications)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Besoin ouvert trouvé"),
        @ApiResponse(responseCode = "404", description = "Besoin introuvable, non publié, clôturé ou dont la date limite est passée")
    })
    @GetMapping("/{besoinId}")
    @ResponseStatus(HttpStatus.OK)
    public BesoinOuvertResponse getBesoinOuvert(@Parameter(description = "Identifiant de l'expression de besoin", required = true) @PathVariable String besoinId) {
        return besoinOuvertService.getBesoinOuvert(besoinId);
    }
}
