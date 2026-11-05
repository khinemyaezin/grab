package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeactivateSellerZoneCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeactivateSellerZoneCommandHandler
        implements CommandHandler<DeactivateSellerZoneCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public DeactivateSellerZoneCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(DeactivateSellerZoneCommand command) {
        outboundPort.deactivateZone(command.zoneId());
        return null;
    }

    @Override
    public Class<DeactivateSellerZoneCommand> getCommandType() {
        return DeactivateSellerZoneCommand.class;
    }
}

