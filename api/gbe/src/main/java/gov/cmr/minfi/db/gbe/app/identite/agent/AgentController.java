package gov.cmr.minfi.db.gbe.app.identite.agent;

import gov.cmr.minfi.db.gbe.app.identite.agent.dto.AgentResponse;
import gov.cmr.minfi.db.gbe.app.identite.agent.dto.CreateAgentRequest;
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
@RequestMapping("/api/v1/admin/agents")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
@Tag(name = OpenApiTags.ADMIN_AGENTS)
public class AgentController {

    private final AgentService agentService;

    @Operation(
        summary = "Créer un agent",
        description = "Enregistre un agent de l'administration."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Agent créé"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse createAgent(@Valid @RequestBody CreateAgentRequest request) {
        return agentService.createAgent(request);
    }

    @Operation(
        summary = "Lister les agents",
        description = "Retourne tous les agents."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des agents"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AgentResponse> getAllAgents() {
        return agentService.getAllsAgents();
    }

    @Operation(
        summary = "Lister les agents sans compte",
        description = "Retourne les agents auxquels aucun compte utilisateur n'est rattaché."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des agents"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante")
    })
    @GetMapping("/without-account")
    @ResponseStatus(HttpStatus.OK)
    public List<AgentResponse> getAgentsWithoutAccount() {
        return agentService.getAgentsWithoutAccount();
    }

    @Operation(
        summary = "Consulter un agent",
        description = "Retourne un agent."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Agent trouvé"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Agent introuvable")
    })
    @GetMapping("/{agentId}")
    @ResponseStatus(HttpStatus.OK)
    public AgentResponse getAgent(@Parameter(description = "Identifiant de l'agent", required = true) @PathVariable String agentId) {
        return agentService.getAgent(agentId);
    }

    @Operation(
        summary = "Désactiver un agent",
        description = "Désactive un agent."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Agent désactivé"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Agent introuvable")
    })
    @PatchMapping("/{agentId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateAgent(@Parameter(description = "Identifiant de l'agent", required = true) @PathVariable String agentId) {
        agentService.deactivateAgent(agentId);
    }

    @Operation(
        summary = "Réactiver un agent",
        description = "Réactive un agent."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Agent réactivé"),
        @ApiResponse(responseCode = "403", description = "Permission MANAGE_USERS manquante"),
        @ApiResponse(responseCode = "404", description = "Agent introuvable")
    })
    @PatchMapping("/{agentId}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivateAgent(@Parameter(description = "Identifiant de l'agent", required = true) @PathVariable String agentId) {
        agentService.reactivateAgent(agentId);
    }
}
