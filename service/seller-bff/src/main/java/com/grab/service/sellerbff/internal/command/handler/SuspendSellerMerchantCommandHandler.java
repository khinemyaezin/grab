package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.SuspendSellerMerchantCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SuspendSellerMerchantCommandHandler
        implements CommandHandler<SuspendSellerMerchantCommand, MerchantOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public SuspendSellerMerchantCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantOutboundResult handle(SuspendSellerMerchantCommand command) {
        return outboundPort.suspendMerchant(command.merchantId(), command.actorId(), command.reason());
    }

    @Override
    public Class<SuspendSellerMerchantCommand> getCommandType() {
        return SuspendSellerMerchantCommand.class;
    }
}

