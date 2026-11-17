package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.UpdateSellerStorefrontCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.UpdateStorefrontRecord;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateSellerStorefrontCommandHandler
        implements CommandHandler<UpdateSellerStorefrontCommand, StorefrontOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public UpdateSellerStorefrontCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public StorefrontOutboundResult handle(UpdateSellerStorefrontCommand command) {
        UpdateStorefrontRecord record = new UpdateStorefrontRecord(
                command.merchantId(),
                command.storefrontId(),
                command.name(),
                command.slug()
        );
        return outboundPort.updateStorefront(record);
    }

    @Override
    public Class<UpdateSellerStorefrontCommand> getCommandType() {
        return UpdateSellerStorefrontCommand.class;
    }
}

