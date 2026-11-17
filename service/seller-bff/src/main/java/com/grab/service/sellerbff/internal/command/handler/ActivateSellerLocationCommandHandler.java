package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.ActivateSellerLocationCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
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

