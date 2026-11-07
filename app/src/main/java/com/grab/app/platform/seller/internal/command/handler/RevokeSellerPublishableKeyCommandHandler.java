package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerSalesChannelCommands.RevokeSellerPublishableKeyCommand;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class RevokeSellerPublishableKeyCommandHandler
        implements CommandHandler<RevokeSellerPublishableKeyCommand, Void> {

    private final SellerSalesChannelOutboundPort outboundPort;

    public RevokeSellerPublishableKeyCommandHandler(SellerSalesChannelOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(RevokeSellerPublishableKeyCommand command) {
        outboundPort.revokePublishableKey(command.salesChannelId(), command.keyId());
        return null;
    }

    @Override
    public Class<RevokeSellerPublishableKeyCommand> getCommandType() {
        return RevokeSellerPublishableKeyCommand.class;
    }
}

