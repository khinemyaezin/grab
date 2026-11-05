package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.AllocateSellerStockCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.AllocateStockRecord;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.AllocationOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class AllocateSellerStockCommandHandler
        implements CommandHandler<AllocateSellerStockCommand, AllocationOutboundResult> {

    private final SellerInventoryOutboundPort outboundPort;

    public AllocateSellerStockCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public AllocationOutboundResult handle(AllocateSellerStockCommand command) {
        AllocateStockRecord record = new AllocateStockRecord(
                command.actorId(),
                command.sku(),
                command.locationId(),
                command.quantity(),
                command.orderId()
        );
        return outboundPort.allocateStock(record);
    }

    @Override
    public Class<AllocateSellerStockCommand> getCommandType() {
        return AllocateSellerStockCommand.class;
    }
}

