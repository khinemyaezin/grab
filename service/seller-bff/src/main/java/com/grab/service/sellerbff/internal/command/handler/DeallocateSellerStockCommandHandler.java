package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.DeallocateSellerStockCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.DeallocateStockRecord;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeallocateSellerStockCommandHandler
        implements CommandHandler<DeallocateSellerStockCommand, Void> {

    private final SellerInventoryOutboundPort outboundPort;

    public DeallocateSellerStockCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(DeallocateSellerStockCommand command) {
        DeallocateStockRecord record = new DeallocateStockRecord(
                command.actorId(),
                command.sku(),
                command.quantity(),
                command.orderId()
        );
        outboundPort.deallocateStock(record);
        return null;
    }

    @Override
    public Class<DeallocateSellerStockCommand> getCommandType() {
        return DeallocateSellerStockCommand.class;
    }
}

