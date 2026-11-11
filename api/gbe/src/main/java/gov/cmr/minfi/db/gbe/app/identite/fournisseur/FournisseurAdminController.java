package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.CreateFournisseurRequest;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.FournisseurResponse;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.RibRequest;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.UpdateFournisseurRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion des fournisseurs par l'administrateur (décision Q5 : pas d'auto-inscription).
 * Activation / désactivation du compte : routes /api/v1/admin/users/{userId}/activate|deactivate.
 */
@RestController
@RequestMapping("/api/v1/admin/fournisseurs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = "Fournisseurs", description = "Gestion des fournisseurs et de leurs RIB")
public class FournisseurAdminController {

    private final FournisseurService fournisseurService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FournisseurResponse createFournisseur(@Valid @RequestBody CreateFournisseurRequest request) {
        return fournisseurService.createFournisseur(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FournisseurResponse> getAllFournisseurs() {
        return fournisseurService.getAllFournisseurs();
    }

    @GetMapping("/{fournisseurId}")
    @ResponseStatus(HttpStatus.OK)
    public FournisseurResponse getFournisseur(@PathVariable String fournisseurId) {
        return fournisseurService.getFournisseur(fournisseurId);
    }

    @PutMapping("/{fournisseurId}")
    @ResponseStatus(HttpStatus.OK)
    public FournisseurResponse updateFournisseur(
            @PathVariable String fournisseurId,
            @Valid @RequestBody UpdateFournisseurRequest request) {
        return fournisseurService.updateFournisseur(fournisseurId, request);
    }

    @PostMapping("/{fournisseurId}/ribs")
    @ResponseStatus(HttpStatus.CREATED)
    public FournisseurResponse ajouterRib(@PathVariable String fournisseurId, @Valid @RequestBody RibRequest request) {
        return fournisseurService.ajouterRib(fournisseurId, request);
    }

    @DeleteMapping("/{fournisseurId}/ribs/{ribId}")
    @ResponseStatus(HttpStatus.OK)
    public FournisseurResponse retirerRib(@PathVariable String fournisseurId, @PathVariable String ribId) {
        return fournisseurService.retirerRib(fournisseurId, ribId);
    }
}
