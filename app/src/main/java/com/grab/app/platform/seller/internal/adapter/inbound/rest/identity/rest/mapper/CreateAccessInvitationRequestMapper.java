package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.CreateAccessInvitationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessInvitationResponse;
import com.identity.application.model.write.AccessInvitationResult;
import com.identity.application.model.write.CreateAccessInvitationCommand;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateAccessInvitationRequestMapper {
    public abstract CreateAccessInvitationCommand toCommand(
            CreateAccessInvitationRequest request,
            String invitedBy,
            String actorScopeKey,
            String actorScopeId,
            Set<String> actorRoleCodes
    );

    public abstract AccessInvitationResponse toResponse(AccessInvitationResult result);
}
