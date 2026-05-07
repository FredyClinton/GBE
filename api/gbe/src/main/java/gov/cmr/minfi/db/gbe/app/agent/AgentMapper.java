package gov.cmr.minfi.db.gbe.app.agent;

import gov.cmr.minfi.db.gbe.app.agent.dto.AgentResponse;
import gov.cmr.minfi.db.gbe.app.agent.dto.CreateAgentRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AgentMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "actif", constant = "true")
    Agent toEntity(CreateAgentRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    AgentResponse toResponse(Agent agent);
}
