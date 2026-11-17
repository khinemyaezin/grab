package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerProductCommands.UpdateSellerProductCommand;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.UpdateProductRecord;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateSellerProductCommandHandler
        implements CommandHandler<UpdateSellerProductCommand, ProductOutboundResult> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public UpdateSellerProductCommandHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public ProductOutboundResult handle(UpdateSellerProductCommand command) {
        UpdateProductRecord record = new UpdateProductRecord(
                command.name(),
                command.categoryId(),
                command.condition(),
                command.slug()
        );
        return outboundPort.updateProduct(command.merchantId(), command.productId(), record);
    }

    @Override
    public Class<UpdateSellerProductCommand> getCommandType() {
        return UpdateSellerProductCommand.class;
    }
}

