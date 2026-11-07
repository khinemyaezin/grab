package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.UpdateSellerProductCommand;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.UpdateProductRecord;
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

