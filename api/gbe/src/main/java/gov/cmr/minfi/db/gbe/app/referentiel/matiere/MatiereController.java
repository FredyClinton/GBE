package gov.cmr.minfi.db.gbe.app.referentiel.matiere;

import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Consultation du catalogue des matières (tout utilisateur authentifié). */
@RestController
@RequestMapping("/api/v1/referentiel/matieres")
@RequiredArgsConstructor
@Tag(name = "Référentiel", description = "Endpoints de consultation du référentiel budgétaire")
public class MatiereController {

    private final MatiereService matiereService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MatiereResponse> getAllMatieres() {
        return matiereService.getAllMatieres();
    }

    @GetMapping("/{matiereId}")
    @ResponseStatus(HttpStatus.OK)
    public MatiereResponse getMatiere(@PathVariable String matiereId) {
        return matiereService.getMatiere(matiereId);
    }
}
