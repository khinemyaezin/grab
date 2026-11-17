package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerProductCommands.CreateSellerProductCommand;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.CreateProductRecord;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
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

