package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.ActivateSellerBinCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ActivateSellerBinCommandHandler
        implements CommandHandler<ActivateSellerBinCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public ActivateSellerBinCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(ActivateSellerBinCommand command) {
        outboundPort.activateBin(command.binId());
        return null;
    }

    @Override
    public Class<ActivateSellerBinCommand> getCommandType() {
        return ActivateSellerBinCommand.class;
    }
}

