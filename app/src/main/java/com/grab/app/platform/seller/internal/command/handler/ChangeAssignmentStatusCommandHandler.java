package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessAssignmentResponse;
import com.grab.app.platform.seller.internal.command.model.SellerIdentityAdminCommands.ChangeAssignmentStatusCommand;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ChangeAssignmentStatusCommandHandler implements CommandHandler<ChangeAssignmentStatusCommand, AccessAssignmentResponse> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public ChangeAssignmentStatusCommandHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public AccessAssignmentResponse handle(ChangeAssignmentStatusCommand command) {
        return outboundPort.changeAssignmentStatus(
                command.assignmentId(),
                command.status(),
                command.actorUserId(),
                command.actorScopeKey(),
                command.actorScopeId(),
                command.actorRoles()
        );
    }

    @Override
    public Class<ChangeAssignmentStatusCommand> getCommandType() {
        return ChangeAssignmentStatusCommand.class;
    }
}

