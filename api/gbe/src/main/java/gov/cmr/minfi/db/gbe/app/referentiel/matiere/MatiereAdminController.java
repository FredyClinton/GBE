package gov.cmr.minfi.db.gbe.app.referentiel.matiere;

import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Gestion du catalogue des matières. Même permission que la gestion des actions. */
@RestController
@RequestMapping("/api/v1/admin/matieres")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = "Matières", description = "Gestion du catalogue des matières")
public class MatiereAdminController {

    private final MatiereService matiereService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatiereResponse createMatiere(@Valid @RequestBody MatiereRequest request) {
        return matiereService.createMatiere(request);
    }

    @PutMapping("/{matiereId}")
    @ResponseStatus(HttpStatus.OK)
    public MatiereResponse updateMatiere(@PathVariable String matiereId, @Valid @RequestBody MatiereRequest request) {
        return matiereService.updateMatiere(matiereId, request);
    }

    @DeleteMapping("/{matiereId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatiere(@PathVariable String matiereId) {
        matiereService.deleteMatiere(matiereId);
    }
}
