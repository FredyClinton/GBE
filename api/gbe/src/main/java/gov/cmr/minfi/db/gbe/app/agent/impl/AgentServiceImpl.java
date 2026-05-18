package gov.cmr.minfi.db.gbe.app.agent.impl;

import gov.cmr.minfi.db.gbe.app.agent.Agent;
import gov.cmr.minfi.db.gbe.app.agent.AgentMapper;
import gov.cmr.minfi.db.gbe.app.agent.AgentRepository;
import gov.cmr.minfi.db.gbe.app.agent.AgentService;
import gov.cmr.minfi.db.gbe.app.agent.dto.AgentResponse;
import gov.cmr.minfi.db.gbe.app.agent.dto.CreateAgentRequest;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;

    @Override
    @Transactional
    public AgentResponse createAgent(CreateAgentRequest request) {
        if (agentRepository.existsByMatriculeIgnoreCase(request.matricule())) {
            throw new BusinessException(ErrorCode.MATRICULE_ALREADY_EXISTS);
        }
        if (agentRepository.existsByNumeroCniIgnoreCase(request.numeroCni())) {
            throw new BusinessException(ErrorCode.CNI_ALREADY_EXISTS);
        }
        if (agentRepository.existsByNuiIgnoreCase(request.nui())) {
            throw new BusinessException(ErrorCode.NUI_ALREADY_EXISTS);
        }
        final Agent agent = agentMapper.toEntity(request);
        agentRepository.save(agent);
        log.info("Created agent {}", agent.getMatricule());
        return agentMapper.toResponse(agent);
    }

    @Override
    public AgentResponse getAgent(String agentId) {
        return agentMapper.toResponse(findAgent(agentId));
    }

    @Override
    public List<AgentResponse> getAllsAgents() {
        return agentRepository.findAll().stream().map(agentMapper::toResponse).toList();
    }

    @Override
    public List<AgentResponse> getAgentsWithoutAccount() {
        return agentRepository.findByUserIsNull().stream().map(agentMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void deactivateAgent(String agentId) {
        final Agent agent = findAgent(agentId);
        if (!agent.isActif()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DEACTIVATED);
        }
        agent.setActif(false);
        agentRepository.save(agent);
        log.info("Deactivated agent {}", agent.getMatricule());
    }

    @Override
    public void reactivateAgent(String agentId) {
        final Agent agent = findAgent(agentId);
        if (agent.isActif()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_ACTIVATED);
        }
        agent.setActif(true);
        agentRepository.save(agent);
        log.info("Activated agent {}", agent.getMatricule());
    }

    private Agent findAgent(String agentId) {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, agentId));
    }
}
