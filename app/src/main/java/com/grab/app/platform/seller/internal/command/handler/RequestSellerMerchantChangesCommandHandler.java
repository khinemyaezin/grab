package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.RequestSellerMerchantChangesCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class RequestSellerMerchantChangesCommandHandler
        implements CommandHandler<RequestSellerMerchantChangesCommand, MerchantOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public RequestSellerMerchantChangesCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantOutboundResult handle(RequestSellerMerchantChangesCommand command) {
        return outboundPort.requestChanges(command.merchantId(), command.actorId(), command.comments());
    }

    @Override
    public Class<RequestSellerMerchantChangesCommand> getCommandType() {
        return RequestSellerMerchantChangesCommand.class;
    }
}

