package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.DeleteSellerProductCommand;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteSellerProductCommandHandler
        implements CommandHandler<DeleteSellerProductCommand, Void> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public DeleteSellerProductCommandHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(DeleteSellerProductCommand command) {
        outboundPort.deleteProduct(command.merchantId(), command.productId());
        return null;
    }

    @Override
    public Class<DeleteSellerProductCommand> getCommandType() {
        return DeleteSellerProductCommand.class;
    }
}

