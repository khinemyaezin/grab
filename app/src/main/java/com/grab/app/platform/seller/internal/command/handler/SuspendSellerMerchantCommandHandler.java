package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.SuspendSellerMerchantCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
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

