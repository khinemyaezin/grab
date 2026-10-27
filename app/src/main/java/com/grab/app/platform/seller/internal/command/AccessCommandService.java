package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.GrantAccessRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessAssignmentResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.ChangeAccessStatusRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.GrantAccessRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.SwitchAccessContextRequestMapper;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.GrantAccessCommand;
import com.identity.application.model.write.SwitchAccessContextCommand;
import com.grab.app.shared.security.SecurityPrincipal;
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
        AuthenticatedAccessScopeResolver.ActorScope actorScope = actorScopes.resolve(principal);
        GrantAccessCommand command = grantMapper.toCommand(
                request,
                principal.getPlatformUserId(),
                actorScope.key(),
                actorScope.id(),
                principal.actor().roles()
        );
        AccessAssignmentResult result = commandBus.dispatch(command);
        return grantMapper.toResponse(result);
    }

    public AccessAssignmentResponse changeStatus(
            String assignmentId,
            AccessAssignmentStatus requestedStatus,
            SecurityPrincipal principal
    ) {
        AuthenticatedAccessScopeResolver.ActorScope actorScope = actorScopes.resolve(principal);
        var command = statusMapper.toCommand(
                assignmentId,
                requestedStatus,
                actorScope.key(),
                actorScope.id(),
                principal.getPlatformUserId(),
                principal.actor().roles()
        );
        AccessAssignmentResult result = commandBus.dispatch(command);
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
