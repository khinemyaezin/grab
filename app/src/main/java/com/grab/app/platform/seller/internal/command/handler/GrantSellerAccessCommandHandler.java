package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessAssignmentResponse;
import com.grab.app.platform.seller.internal.command.model.SellerIdentityAdminCommands.GrantAccessCommand;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class GrantSellerAccessCommandHandler implements CommandHandler<GrantAccessCommand, AccessAssignmentResponse> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public GrantSellerAccessCommandHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public AccessAssignmentResponse handle(GrantAccessCommand command) {
        return outboundPort.grantAccess(
                command.request(),
                command.actorUserId(),
                command.actorScopeKey(),
                command.actorScopeId(),
                command.actorRoles()
        );
    }

    @Override
    public Class<GrantAccessCommand> getCommandType() {
        return GrantAccessCommand.class;
    }
}

