package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.StartSellerMerchantApplicationCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StartApplicationRecord;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartSellerMerchantApplicationCommandHandler
        implements CommandHandler<StartSellerMerchantApplicationCommand, MerchantOutboundResult> {

    private final SellerMerchantOutboundPort outboundPort;

    public StartSellerMerchantApplicationCommandHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public MerchantOutboundResult handle(StartSellerMerchantApplicationCommand command) {
        StartApplicationRecord record = new StartApplicationRecord(
                command.actorId(),
                command.businessType(),
                command.displayName(),
                command.primaryMarketCountryCode()
        );
        return outboundPort.startApplication(record);
    }

    @Override
    public Class<StartSellerMerchantApplicationCommand> getCommandType() {
        return StartSellerMerchantApplicationCommand.class;
    }
}

