package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.DeleteSellerZoneCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
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

