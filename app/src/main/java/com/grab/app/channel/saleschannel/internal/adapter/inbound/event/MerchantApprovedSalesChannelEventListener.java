package com.grab.app.channel.saleschannel.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.shared.events.merchant.MerchantApprovedIntegrationEvent;
import com.grab.app.channel.saleschannel.internal.adapter.inbound.event.mapper.ProvisionMerchantWebsiteChannelCommandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantApprovedSalesChannelEventListener {
    private final CommandBus commandBus;
    private final ProvisionMerchantWebsiteChannelCommandMapper mapper;

    @EventListener
    public void onMerchantApproved(MerchantApprovedIntegrationEvent event) {
        commandBus.dispatch(mapper.toCommand(event));
    }
}
