package gov.cmr.minfi.db.gbe.app.agent;

import gov.cmr.minfi.db.gbe.app.agent.dto.AgentResponse;
import gov.cmr.minfi.db.gbe.app.agent.dto.CreateAgentRequest;
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
@Tag(name = "Agents", description = "Gestion des agents de l'administration")
public class AgentController {

    private final AgentService agentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse createAgent(@Valid @RequestBody CreateAgentRequest request) {
        return agentService.createAgent(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AgentResponse> getAllAgents() {
        return agentService.getAllsAgents();
    }

    @GetMapping("/without-account")
    @ResponseStatus(HttpStatus.OK)
    public List<AgentResponse> getAgentsWithoutAccount() {
        return agentService.getAgentsWithoutAccount();
    }

    @GetMapping("/{agentId}")
    @ResponseStatus(HttpStatus.OK)
    public AgentResponse getAgent(@PathVariable String agentId) {
        return agentService.getAgent(agentId);
    }

    @PatchMapping("/{agentId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateAgent(@PathVariable String agentId) {
        agentService.deactivateAgent(agentId);
    }

    @PatchMapping("/{agentId}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivateAgent(@PathVariable String agentId) {
        agentService.reactivateAgent(agentId);
    }
}