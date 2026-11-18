package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerPricingCommands.CreateSellerPricePreferenceCommand;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.CreatePricePreferenceRecord;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
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
