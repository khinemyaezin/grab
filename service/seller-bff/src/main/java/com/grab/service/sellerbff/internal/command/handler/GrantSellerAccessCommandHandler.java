package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.AccessAssignmentResponse;
import com.grab.service.sellerbff.internal.command.model.SellerIdentityAdminCommands.GrantAccessCommand;
import com.grab.service.sellerbff.port.outbound.SellerIdentityAdminOutboundPort;
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

