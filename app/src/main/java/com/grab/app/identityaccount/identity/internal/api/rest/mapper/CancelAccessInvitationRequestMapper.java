package com.grab.app.identityaccount.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.identityaccount.identity.internal.api.rest.dto.response.AccessInvitationResponse;
import com.identity.application.model.write.AccessInvitationResult;
import com.identity.application.model.write.CancelAccessInvitationCommand;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CancelAccessInvitationRequestMapper {
    public abstract CancelAccessInvitationCommand toCommand(
            String invitationId,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoleCodes
    );

    public abstract AccessInvitationResponse toResponse(AccessInvitationResult result);
}
