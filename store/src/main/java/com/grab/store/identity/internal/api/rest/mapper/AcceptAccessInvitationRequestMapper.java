package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.request.AcceptAccessInvitationRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AccessAssignmentResponse;
import com.grab.store.identity.internal.command.AcceptAccessInvitationCommand;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AcceptAccessInvitationRequestMapper {
    public abstract AcceptAccessInvitationCommand toCommand(
            AcceptAccessInvitationRequest request,
            String userId,
            String userEmail
    );

    public abstract AccessAssignmentResponse toResponse(AccessAssignmentResult result);
}
