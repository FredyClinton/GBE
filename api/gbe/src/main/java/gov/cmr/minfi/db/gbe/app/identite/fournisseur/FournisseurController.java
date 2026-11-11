package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.FournisseurResponse;
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
@Tag(name = "Espace fournisseur", description = "Routes du fournisseur connecté")
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @GetMapping("/moi")
    @ResponseStatus(HttpStatus.OK)
    public FournisseurResponse getMonProfil() {
        return fournisseurService.getMonProfil();
    }
}
