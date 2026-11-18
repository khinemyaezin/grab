package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.DeactivateSellerLocationCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeactivateSellerLocationCommandHandler
        implements CommandHandler<DeactivateSellerLocationCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public DeactivateSellerLocationCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(DeactivateSellerLocationCommand command) {
        outboundPort.deactivateLocation(command.locationId());
        return null;
    }

    @Override
    public Class<DeactivateSellerLocationCommand> getCommandType() {
        return DeactivateSellerLocationCommand.class;
    }
}

