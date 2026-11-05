package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.ActivateSellerLocationCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ActivateSellerLocationCommandHandler
        implements CommandHandler<ActivateSellerLocationCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public ActivateSellerLocationCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(ActivateSellerLocationCommand command) {
        outboundPort.activateLocation(command.locationId());
        return null;
    }

    @Override
    public Class<ActivateSellerLocationCommand> getCommandType() {
        return ActivateSellerLocationCommand.class;
    }
}

