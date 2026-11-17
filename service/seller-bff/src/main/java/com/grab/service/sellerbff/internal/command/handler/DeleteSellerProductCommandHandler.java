package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerProductCommands.DeleteSellerProductCommand;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort;
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

