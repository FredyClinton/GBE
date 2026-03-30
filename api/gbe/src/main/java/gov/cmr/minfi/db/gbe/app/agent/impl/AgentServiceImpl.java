package gov.cmr.minfi.db.gbe.app.agent.impl;

import gov.cmr.minfi.db.gbe.app.agent.Agent;
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
        final Agent agent = Agent.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .matricule(request.matricule())
                .nui(request.nui())
                .numeroCni(request.numeroCni())
                .cniIssueDate(request.cniIssueDate())
                .cniExpiryDate(request.cniExpiryDate())
                .phoneNumber(request.phoneNumber())
                .actif(true)
                .build();
        agentRepository.save(agent);
        log.info("Created agent {}", agent.getMatricule());
        return toResponse(agent);
    }

    @Override
    public AgentResponse getAgent(String agentId) {
        return toResponse(findAgent(agentId));
    }

    @Override
    public List<AgentResponse> getAllsAgents() {
        return agentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
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

    private AgentResponse toResponse(Agent agent) {
        return AgentResponse.builder()
                .id(agent.getId())
                .firstName(agent.getFirstName())
                .lastName(agent.getLastName())
                .dateOfBirth(agent.getDateOfBirth())
                .matricule(agent.getMatricule())
                .nui(agent.getNui())
                .phoneNumber(agent.getPhoneNumber())
                .numeroCni(agent.getNui())
                .cniIssueDate(agent.getCniIssueDate())
                .cniExpiryDate(agent.getCniExpiryDate())
                .actif(agent.isActif())
                .userId(agent.getUser() != null ? agent.getUser().getId() : null)
                .email(agent.getUser() != null ? agent.getUser().getEmail() : null)
                .build();
    }
}
