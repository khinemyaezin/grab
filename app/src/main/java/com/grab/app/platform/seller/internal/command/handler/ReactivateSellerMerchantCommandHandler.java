package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.ReactivateSellerMerchantCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ReactivateSellerMerchantCommandHandler
        implements CommandHandler<ReactivateSellerMerchantCommand, MerchantOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public ReactivateSellerMerchantCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantOutboundResult handle(ReactivateSellerMerchantCommand command) {
        return outboundPort.reactivateMerchant(command.merchantId(), command.actorId());
    }

    @Override
    public Class<ReactivateSellerMerchantCommand> getCommandType() {
        return ReactivateSellerMerchantCommand.class;
    }
}

