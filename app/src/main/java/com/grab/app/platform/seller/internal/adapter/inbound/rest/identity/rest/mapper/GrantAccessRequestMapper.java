package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.GrantAccessRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessAssignmentResponse;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.GrantAccessCommand;
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
