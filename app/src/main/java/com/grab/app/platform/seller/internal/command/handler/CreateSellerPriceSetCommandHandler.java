package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerPricingCommands.CreateSellerPriceSetCommand;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerPriceSetCommandHandler
        implements CommandHandler<CreateSellerPriceSetCommand, PriceSetOutboundResult> {

    private final SellerPricingOutboundPort outboundPort;

    public CreateSellerPriceSetCommandHandler(SellerPricingOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public PriceSetOutboundResult handle(CreateSellerPriceSetCommand command) {
        return outboundPort.createPriceSet();
    }

    @Override
    public Class<CreateSellerPriceSetCommand> getCommandType() {
        return CreateSellerPriceSetCommand.class;
    }
}

