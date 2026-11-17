package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.RejectSellerMerchantCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class RejectSellerMerchantCommandHandler
        implements CommandHandler<RejectSellerMerchantCommand, MerchantOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public RejectSellerMerchantCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantOutboundResult handle(RejectSellerMerchantCommand command) {
        return outboundPort.rejectMerchant(command.merchantId(), command.actorId(), command.reason());
    }

    @Override
    public Class<RejectSellerMerchantCommand> getCommandType() {
        return RejectSellerMerchantCommand.class;
    }
}

