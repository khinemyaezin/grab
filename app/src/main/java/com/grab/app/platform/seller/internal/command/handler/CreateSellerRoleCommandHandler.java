package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RoleResponse;
import com.grab.app.platform.seller.internal.command.model.SellerIdentityAdminCommands.CreateRoleCommand;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerRoleCommandHandler implements CommandHandler<CreateRoleCommand, RoleResponse> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public CreateSellerRoleCommandHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public RoleResponse handle(CreateRoleCommand command) {
        return outboundPort.createRole(command.request());
    }

    @Override
    public Class<CreateRoleCommand> getCommandType() {
        return CreateRoleCommand.class;
    }
}

