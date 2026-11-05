package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.CloseSellerStorefrontCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CloseSellerStorefrontCommandHandler
        implements CommandHandler<CloseSellerStorefrontCommand, Void> {

    private final SellerMerchantOutboundPort outboundPort;

    public CloseSellerStorefrontCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(CloseSellerStorefrontCommand command) {
        outboundPort.closeStorefront(command.merchantId(), command.storefrontId());
        return null;
    }

    @Override
    public Class<CloseSellerStorefrontCommand> getCommandType() {
        return CloseSellerStorefrontCommand.class;
    }
}

