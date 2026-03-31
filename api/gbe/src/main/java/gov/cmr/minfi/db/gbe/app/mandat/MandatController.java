package gov.cmr.minfi.db.gbe.app.mandat;

import gov.cmr.minfi.db.gbe.app.mandat.dto.CreateMandatRequest;
import gov.cmr.minfi.db.gbe.app.mandat.dto.MandatSummary;
import gov.cmr.minfi.db.gbe.app.mandat.dto.UpdateMandatRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/utilisateurs/{userId}/mandats")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_AFFECTATIONS')")
@Tag(name = "Mandats", description = "Gestion des mandats budgétaires")
public class MandatController {

    private final MandatService mandatService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MandatSummary createMandat(
            @PathVariable String userId,
            @Valid @RequestBody CreateMandatRequest request
    ) {
        return mandatService.createMandat(userId, request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MandatSummary> getMandats(@PathVariable String userId) {
        return mandatService.getMandats(userId);
    }

    @PatchMapping("/{mandatId}")
    @ResponseStatus(HttpStatus.OK)
    public MandatSummary updateMandat(
            @PathVariable String userId,
            @PathVariable String mandatId,
            @Valid @RequestBody UpdateMandatRequest request
    ) {
        return mandatService.updateMandat(userId, mandatId, request);
    }

    @PatchMapping("/{mandatId}/activer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateMandat(
            @PathVariable String userId,
            @PathVariable String mandatId
    ) {
        mandatService.activateMandat(userId, mandatId);
    }

    @PatchMapping("/{mandatId}/desactiver")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateMandat(
            @PathVariable String userId,
            @PathVariable String mandatId
    ) {
        mandatService.deactivateMandat(userId, mandatId);
    }

    @DeleteMapping("/{mandatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMandat(
            @PathVariable String userId,
            @PathVariable String mandatId
    ) {
        mandatService.removeMandat(userId, mandatId);
    }
}