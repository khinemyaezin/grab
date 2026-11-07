package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerPricingCommands.CreateSellerPriceListCommand;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CreatePriceListRecord;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerPriceListCommandHandler
        implements CommandHandler<CreateSellerPriceListCommand, PriceListOutboundResult> {

    private final SellerPricingOutboundPort outboundPort;

    public CreateSellerPriceListCommandHandler(SellerPricingOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public PriceListOutboundResult handle(CreateSellerPriceListCommand command) {
        CreatePriceListRecord record = new CreatePriceListRecord(
                command.title(),
                command.description(),
                command.type(),
                command.startsAt(),
                command.endsAt()
        );
        return outboundPort.createPriceList(record);
    }

    @Override
    public Class<CreateSellerPriceListCommand> getCommandType() {
        return CreateSellerPriceListCommand.class;
    }
}

