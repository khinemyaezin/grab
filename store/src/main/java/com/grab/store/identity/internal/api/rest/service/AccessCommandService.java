package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.identity.internal.api.rest.dto.request.GrantAccessRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AccessAssignmentResponse;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.api.rest.mapper.ChangeAccessStatusRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.GrantAccessRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.SwitchAccessContextRequestMapper;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.command.AuthResult;
import com.grab.store.identity.internal.command.SwitchAccessContextCommand;
import com.grab.store.shared.security.SecurityPrincipal;
import com.identity.domain.enums.AccessAssignmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessCommandService {
    private final CommandBus commandBus;
    private final GrantAccessRequestMapper grantMapper;
    private final ChangeAccessStatusRequestMapper statusMapper;
    private final SwitchAccessContextRequestMapper switchMapper;
    private final AuthenticatedAccessScopeResolver actorScopes;

    public AccessAssignmentResponse grant(GrantAccessRequest request, SecurityPrincipal principal) {
        var actorScope = actorScopes.resolve(principal);
        AccessAssignmentResult result = commandBus.dispatch(grantMapper.toCommand(
                request,
                principal.getPlatformUserId(),
                actorScope.key(),
                actorScope.id(),
                principal.actor().roles()
        ));
        return grantMapper.toResponse(result);
    }

    public AccessAssignmentResponse changeStatus(
            String assignmentId,
            AccessAssignmentStatus requestedStatus,
            SecurityPrincipal principal
    ) {
        var actorScope = actorScopes.resolve(principal);
        AccessAssignmentResult result = commandBus.dispatch(statusMapper.toCommand(
                assignmentId,
                requestedStatus,
                actorScope.key(),
                actorScope.id(),
                principal.getPlatformUserId(),
                principal.actor().roles()
        ));
        return statusMapper.toResponse(result);
    }

    public AuthResponse switchContext(
            String assignmentId,
            String currentRefreshToken,
            SecurityPrincipal principal
    ) {
        SwitchAccessContextCommand command = switchMapper.toCommand(
                principal.getPlatformUserId(),
                assignmentId,
                currentRefreshToken
        );
        AuthResult result = commandBus.dispatch(command);
        return switchMapper.toResponse(result);
    }
}
