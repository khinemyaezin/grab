package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.request.GrantAccessRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AccessAssignmentResponse;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.command.GrantAccessCommand;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GrantAccessRequestMapper {
    public abstract GrantAccessCommand toCommand(
            GrantAccessRequest request,
            String assignedBy,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoleCodes
    );

    public abstract AccessAssignmentResponse toResponse(AccessAssignmentResult result);
}
