package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.ActivateSellerZoneCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ActivateSellerZoneCommandHandler
        implements CommandHandler<ActivateSellerZoneCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public ActivateSellerZoneCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(ActivateSellerZoneCommand command) {
        outboundPort.activateZone(command.zoneId());
        return null;
    }

    @Override
    public Class<ActivateSellerZoneCommand> getCommandType() {
        return ActivateSellerZoneCommand.class;
    }
}

