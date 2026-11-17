package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerSalesChannelCommands.RevokeSellerPublishableKeyCommand;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort;
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

