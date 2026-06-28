package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.AccessAssignmentResponse;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.command.ChangeAccessStatusCommand;
import com.identity.domain.enums.AccessAssignmentStatus;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeAccessStatusRequestMapper {
    public abstract ChangeAccessStatusCommand toCommand(
            String assignmentId,
            AccessAssignmentStatus requestedStatus,
            String actorScopeType,
            String actorScopeId,
            String actorId,
            Set<String> actorRoleCodes
    );

    public abstract AccessAssignmentResponse toResponse(AccessAssignmentResult result);
}
