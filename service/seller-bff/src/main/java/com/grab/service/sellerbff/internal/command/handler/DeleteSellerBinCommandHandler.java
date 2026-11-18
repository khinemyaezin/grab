package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.DeleteSellerBinCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
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

