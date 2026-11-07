package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeleteSellerLocationCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteSellerLocationCommandHandler
        implements CommandHandler<DeleteSellerLocationCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public DeleteSellerLocationCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(DeleteSellerLocationCommand command) {
        outboundPort.deleteLocation(command.locationId());
        return null;
    }

    @Override
    public Class<DeleteSellerLocationCommand> getCommandType() {
        return DeleteSellerLocationCommand.class;
    }
}

