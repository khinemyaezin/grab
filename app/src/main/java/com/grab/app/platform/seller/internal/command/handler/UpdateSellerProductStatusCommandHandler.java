package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.UpdateSellerProductStatusCommand;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateSellerProductStatusCommandHandler
        implements CommandHandler<UpdateSellerProductStatusCommand, ProductOutboundResult> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public UpdateSellerProductStatusCommandHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public ProductOutboundResult handle(UpdateSellerProductStatusCommand command) {
        return outboundPort.updateProductStatus(command.merchantId(), command.productId(), command.status());
    }

    @Override
    public Class<UpdateSellerProductStatusCommand> getCommandType() {
        return UpdateSellerProductStatusCommand.class;
    }
}

