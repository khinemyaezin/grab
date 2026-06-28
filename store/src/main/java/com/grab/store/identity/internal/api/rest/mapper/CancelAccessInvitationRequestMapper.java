package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.AccessInvitationResponse;
import com.grab.store.identity.internal.command.AccessInvitationResult;
import com.grab.store.identity.internal.command.CancelAccessInvitationCommand;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CancelAccessInvitationRequestMapper {
    public abstract CancelAccessInvitationCommand toCommand(
            String invitationId,
            String actorScopeType,
            String actorScopeId,
            Set<String> actorRoleCodes
    );

    public abstract AccessInvitationResponse toResponse(AccessInvitationResult result);
}
