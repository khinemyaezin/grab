package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.AccessAssignmentPort.AssignmentMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.read.ListAccessAssignmentsQuery;
import com.identity.application.model.write.AccessAssignmentResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListAccessAssignmentsPortMapper {

    public ListAccessAssignmentsQuery toQuery(Id userId, String actorScopeKey, String actorScopeId) {
        return new ListAccessAssignmentsQuery(userId, actorScopeKey, actorScopeId);
    }

    public abstract AssignmentMessageResult toResponse(AccessAssignmentResult result);
}
