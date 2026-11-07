package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerInventoryItemCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.CreateItemRecord;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerInventoryItemCommandHandler
        implements CommandHandler<CreateSellerInventoryItemCommand, InventoryItemOutboundResult> {

    private final SellerInventoryOutboundPort outboundPort;

    public CreateSellerInventoryItemCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public InventoryItemOutboundResult handle(CreateSellerInventoryItemCommand command) {
        CreateItemRecord record = new CreateItemRecord(
                command.merchantId(),
                command.actorId(),
                command.sku(),
                command.locationId(),
                command.initialQuantity(),
                command.safetyStock(),
                command.reorderPoint(),
                command.reorderQuantity(),
                command.maxStock()
        );
        return outboundPort.createItem(record);
    }

    @Override
    public Class<CreateSellerInventoryItemCommand> getCommandType() {
        return CreateSellerInventoryItemCommand.class;
    }
}

