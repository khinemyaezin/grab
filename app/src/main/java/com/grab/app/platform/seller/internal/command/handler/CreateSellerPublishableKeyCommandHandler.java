package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerSalesChannelCommands.CreateSellerPublishableKeyCommand;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.CreatePublishableKeyRecord;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.PublishableKeyOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerPublishableKeyCommandHandler
        implements CommandHandler<CreateSellerPublishableKeyCommand, PublishableKeyOutboundResult> {

    private final SellerSalesChannelOutboundPort outboundPort;

    public CreateSellerPublishableKeyCommandHandler(SellerSalesChannelOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public PublishableKeyOutboundResult handle(CreateSellerPublishableKeyCommand command) {
        CreatePublishableKeyRecord record = new CreatePublishableKeyRecord(
                command.salesChannelId(),
                command.label()
        );
        return outboundPort.createPublishableKey(record);
    }

    @Override
    public Class<CreateSellerPublishableKeyCommand> getCommandType() {
        return CreateSellerPublishableKeyCommand.class;
    }
}

