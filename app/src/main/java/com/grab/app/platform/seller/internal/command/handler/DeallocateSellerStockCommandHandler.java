package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeallocateSellerStockCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.DeallocateStockRecord;
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

