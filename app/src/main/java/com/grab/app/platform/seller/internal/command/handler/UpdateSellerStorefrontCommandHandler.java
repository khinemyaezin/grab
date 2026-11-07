package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.UpdateSellerStorefrontCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.UpdateStorefrontRecord;
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

