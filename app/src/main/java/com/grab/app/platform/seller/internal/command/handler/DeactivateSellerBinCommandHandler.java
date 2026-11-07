package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeactivateSellerBinCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeactivateSellerBinCommandHandler
        implements CommandHandler<DeactivateSellerBinCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public DeactivateSellerBinCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(DeactivateSellerBinCommand command) {
        outboundPort.deactivateBin(command.binId());
        return null;
    }

    @Override
    public Class<DeactivateSellerBinCommand> getCommandType() {
        return DeactivateSellerBinCommand.class;
    }
}

