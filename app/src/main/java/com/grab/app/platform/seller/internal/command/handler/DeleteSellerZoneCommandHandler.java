package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeleteSellerZoneCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteSellerZoneCommandHandler
        implements CommandHandler<DeleteSellerZoneCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public DeleteSellerZoneCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(DeleteSellerZoneCommand command) {
        outboundPort.deleteZone(command.zoneId());
        return null;
    }

    @Override
    public Class<DeleteSellerZoneCommand> getCommandType() {
        return DeleteSellerZoneCommand.class;
    }
}

