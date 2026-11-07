package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.CreateSellerProductCommand;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.CreateProductRecord;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerProductCommandHandler
        implements CommandHandler<CreateSellerProductCommand, ProductOutboundResult> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public CreateSellerProductCommandHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public ProductOutboundResult handle(CreateSellerProductCommand command) {
        CreateProductRecord record = new CreateProductRecord(
                command.merchantId(),
                command.name(),
                command.categoryId(),
                command.condition(),
                command.slug(),
                command.status(),
                command.variants()
        );
        return outboundPort.createProduct(record);
    }

    @Override
    public Class<CreateSellerProductCommand> getCommandType() {
        return CreateSellerProductCommand.class;
    }
}

