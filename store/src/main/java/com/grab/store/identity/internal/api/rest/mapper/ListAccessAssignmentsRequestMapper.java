package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.AccessAssignmentResponse;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.query.ListAccessAssignmentsQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListAccessAssignmentsRequestMapper {
    public abstract ListAccessAssignmentsQuery toQuery(
            String userId,
            String actorScopeType,
            String actorScopeId
    );

    public abstract AccessAssignmentResponse toResponse(AccessAssignmentResult result);
}
