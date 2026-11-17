package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerAuthCommands.SellerLogoutCommand;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SellerLogoutCommandHandler implements CommandHandler<SellerLogoutCommand, Void> {

    private final SellerAuthOutboundPort outboundPort;

    public SellerLogoutCommandHandler(SellerAuthOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(SellerLogoutCommand command) {
        outboundPort.logout(command.refreshToken());
        return null;
    }

    @Override
    public Class<SellerLogoutCommand> getCommandType() {
        return SellerLogoutCommand.class;
    }
}

