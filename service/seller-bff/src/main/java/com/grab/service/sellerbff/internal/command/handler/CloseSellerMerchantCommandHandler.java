package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.CloseSellerMerchantCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CloseSellerMerchantCommandHandler
        implements CommandHandler<CloseSellerMerchantCommand, MerchantOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public CloseSellerMerchantCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantOutboundResult handle(CloseSellerMerchantCommand command) {
        return outboundPort.closeMerchant(command.merchantId(), command.actorId());
    }

    @Override
    public Class<CloseSellerMerchantCommand> getCommandType() {
        return CloseSellerMerchantCommand.class;
    }
}

