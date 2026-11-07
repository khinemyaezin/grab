package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.CreateSellerStorefrontCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.CreateStorefrontRecord;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerStorefrontCommandHandler
        implements CommandHandler<CreateSellerStorefrontCommand, StorefrontOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public CreateSellerStorefrontCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public StorefrontOutboundResult handle(CreateSellerStorefrontCommand command) {
        CreateStorefrontRecord record = new CreateStorefrontRecord(
                command.merchantId(),
                command.name(),
                command.slug()
        );
        return outboundPort.createStorefront(record);
    }

    @Override
    public Class<CreateSellerStorefrontCommand> getCommandType() {
        return CreateSellerStorefrontCommand.class;
    }
}

