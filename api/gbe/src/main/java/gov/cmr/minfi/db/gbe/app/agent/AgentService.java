package gov.cmr.minfi.db.gbe.app.agent;


import gov.cmr.minfi.db.gbe.app.agent.dto.AgentResponse;
import gov.cmr.minfi.db.gbe.app.agent.dto.CreateAgentRequest;

import java.util.List;

public interface AgentService {
    AgentResponse createAgent(CreateAgentRequest request);

    AgentResponse getAgent(String agentId);

    List<AgentResponse> getAllsAgents();

    List<AgentResponse> getAgentsWithoutAccount();

    void deactivateAgent(String agentId);

    void reactivateAgent(String agentId);
}
