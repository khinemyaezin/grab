package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.ActivateSellerZoneCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
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

