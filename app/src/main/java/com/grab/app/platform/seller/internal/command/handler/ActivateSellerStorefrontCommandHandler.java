package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.ActivateSellerStorefrontCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ActivateSellerStorefrontCommandHandler
        implements CommandHandler<ActivateSellerStorefrontCommand, Void> {

    private final SellerMerchantOutboundPort outboundPort;

    public ActivateSellerStorefrontCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(ActivateSellerStorefrontCommand command) {
        outboundPort.activateStorefront(command.merchantId(), command.storefrontId());
        return null;
    }

    @Override
    public Class<ActivateSellerStorefrontCommand> getCommandType() {
        return ActivateSellerStorefrontCommand.class;
    }
}

