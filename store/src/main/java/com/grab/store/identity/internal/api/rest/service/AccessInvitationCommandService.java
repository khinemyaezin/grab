package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.identity.internal.api.rest.dto.request.AcceptAccessInvitationRequest;
import com.grab.store.identity.internal.api.rest.dto.request.CreateAccessInvitationRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AccessAssignmentResponse;
import com.grab.store.identity.internal.api.rest.dto.response.AccessInvitationResponse;
import com.grab.store.identity.internal.api.rest.mapper.AcceptAccessInvitationRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.CreateAccessInvitationRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.CancelAccessInvitationRequestMapper;
import com.grab.store.identity.internal.command.*;
import com.grab.store.shared.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessInvitationCommandService {
    private final CommandBus commandBus;
    private final CreateAccessInvitationRequestMapper createMapper;
    private final AcceptAccessInvitationRequestMapper acceptMapper;
    private final CancelAccessInvitationRequestMapper cancelMapper;
    private final AuthenticatedAccessScopeResolver actorScopes;

    public AccessInvitationResponse create(
            CreateAccessInvitationRequest request,
            SecurityPrincipal principal
    ) {
        AuthenticatedAccessScopeResolver.ActorScope actorScope = actorScopes.resolve(principal);
        CreateAccessInvitationCommand command = createMapper.toCommand(
                request,
                principal.getPlatformUserId(),
                actorScope.type(),
                actorScope.id(),
                principal.actor().roles()
        );
        AccessInvitationResult result = commandBus.dispatch(command);
        return createMapper.toResponse(result);
    }

    public AccessAssignmentResponse accept(
            AcceptAccessInvitationRequest request,
            SecurityPrincipal principal
    ) {
        AcceptAccessInvitationCommand command = acceptMapper.toCommand(
                request,
                principal.getPlatformUserId(),
                principal.getUsername()
        );
        AccessAssignmentResult result = commandBus.dispatch(command);
        return acceptMapper.toResponse(result);
    }

    public AccessInvitationResponse cancel(String invitationId, SecurityPrincipal principal) {
        AuthenticatedAccessScopeResolver.ActorScope actorScope = actorScopes.resolve(principal);
        CancelAccessInvitationCommand command = cancelMapper.toCommand(
                invitationId,
                actorScope.type(),
                actorScope.id(),
                principal.actor().roles()
        );
        AccessInvitationResult result = commandBus.dispatch(command);
        return cancelMapper.toResponse(result);
    }
}
