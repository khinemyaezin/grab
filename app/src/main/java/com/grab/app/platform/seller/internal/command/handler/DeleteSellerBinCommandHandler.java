package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeleteSellerBinCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteSellerBinCommandHandler
        implements CommandHandler<DeleteSellerBinCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public DeleteSellerBinCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(DeleteSellerBinCommand command) {
        outboundPort.deleteBin(command.binId());
        return null;
    }

    @Override
    public Class<DeleteSellerBinCommand> getCommandType() {
        return DeleteSellerBinCommand.class;
    }
}

