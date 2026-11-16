package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerIdentityAdminCommands.ChangeUserStatusCommand;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ChangeSellerUserStatusCommandHandler implements CommandHandler<ChangeUserStatusCommand, Void> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public ChangeSellerUserStatusCommandHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(ChangeUserStatusCommand command) {
        outboundPort.changeUserStatus(command.userId(), command.status());
        return null;
    }

    @Override
    public Class<ChangeUserStatusCommand> getCommandType() {
        return ChangeUserStatusCommand.class;
    }
}

