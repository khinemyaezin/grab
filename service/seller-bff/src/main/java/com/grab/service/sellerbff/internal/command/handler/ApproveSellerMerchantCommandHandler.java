package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.ApproveSellerMerchantCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
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

