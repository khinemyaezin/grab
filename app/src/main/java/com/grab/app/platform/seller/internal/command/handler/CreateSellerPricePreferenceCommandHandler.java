package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerPricingCommands.CreateSellerPricePreferenceCommand;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CreatePricePreferenceRecord;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerPricePreferenceCommandHandler
        implements CommandHandler<CreateSellerPricePreferenceCommand, PricePreferenceOutboundResult> {

    private final SellerPricingOutboundPort outboundPort;

    public CreateSellerPricePreferenceCommandHandler(SellerPricingOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public PricePreferenceOutboundResult handle(CreateSellerPricePreferenceCommand command) {
        CreatePricePreferenceRecord record = new CreatePricePreferenceRecord(
                command.attribute(),
                command.value(),
                command.taxInclusive()
        );
        return outboundPort.createPricePreference(record);
    }

    @Override
    public Class<CreateSellerPricePreferenceCommand> getCommandType() {
        return CreateSellerPricePreferenceCommand.class;
    }
}
