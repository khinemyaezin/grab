package com.grab.app.channel.saleschannel.internal.adapter.inbound.event;

import com.grab.app.shared.events.merchant.MerchantApprovedIntegrationEvent;
import com.grab.app.channel.saleschannel.internal.adapter.inbound.event.mapper.ProvisionMerchantWebsiteChannelCommandMapper;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MerchantApprovedSalesChannelEventListener {
    private final CommandBus commandBus;
    private final ProvisionMerchantWebsiteChannelCommandMapper mapper;

    public MerchantApprovedSalesChannelEventListener(
            @Qualifier("channelCommandBus") CommandBus commandBus,
            ProvisionMerchantWebsiteChannelCommandMapper mapper
    ) {
        this.commandBus = commandBus;
        this.mapper = mapper;
    }

    @EventListener
    public void onMerchantApproved(MerchantApprovedIntegrationEvent event) {
        commandBus.dispatch(mapper.toCommand(event));
    }
}
