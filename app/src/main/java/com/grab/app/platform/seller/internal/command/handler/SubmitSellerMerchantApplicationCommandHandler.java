package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.SubmitSellerMerchantApplicationCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SubmitSellerMerchantApplicationCommandHandler
        implements CommandHandler<SubmitSellerMerchantApplicationCommand, MerchantOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public SubmitSellerMerchantApplicationCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantOutboundResult handle(SubmitSellerMerchantApplicationCommand command) {
        return outboundPort.submitApplication(command.applicationId(), command.actorId());
    }

    @Override
    public Class<SubmitSellerMerchantApplicationCommand> getCommandType() {
        return SubmitSellerMerchantApplicationCommand.class;
    }
}

