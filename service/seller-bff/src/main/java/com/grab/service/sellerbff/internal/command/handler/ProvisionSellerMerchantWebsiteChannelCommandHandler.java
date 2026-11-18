package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerSalesChannelCommands.ProvisionSellerMerchantWebsiteChannelCommand;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerSalesChannelOutboundPort.ProvisionOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ProvisionSellerMerchantWebsiteChannelCommandHandler
        implements CommandHandler<ProvisionSellerMerchantWebsiteChannelCommand, ProvisionOutboundResult> {

    private final SellerSalesChannelOutboundPort outboundPort;

    public ProvisionSellerMerchantWebsiteChannelCommandHandler(SellerSalesChannelOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public ProvisionOutboundResult handle(ProvisionSellerMerchantWebsiteChannelCommand command) {
        return outboundPort.provisionMerchantWebsiteChannel(command.merchantId(), command.countryCode());
    }

    @Override
    public Class<ProvisionSellerMerchantWebsiteChannelCommand> getCommandType() {
        return ProvisionSellerMerchantWebsiteChannelCommand.class;
    }
}

