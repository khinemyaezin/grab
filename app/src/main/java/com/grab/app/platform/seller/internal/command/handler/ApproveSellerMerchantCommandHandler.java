package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.ApproveSellerMerchantCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ApproveSellerMerchantCommandHandler
        implements CommandHandler<ApproveSellerMerchantCommand, MerchantOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public ApproveSellerMerchantCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantOutboundResult handle(ApproveSellerMerchantCommand command) {
        return outboundPort.approveMerchant(command.merchantId(), command.actorId());
    }

    @Override
    public Class<ApproveSellerMerchantCommand> getCommandType() {
        return ApproveSellerMerchantCommand.class;
    }
}

