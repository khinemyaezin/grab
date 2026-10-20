package com.grab.store.saleschannel.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.shared.events.merchant.MerchantApprovedIntegrationEvent;
import com.grab.store.saleschannel.internal.event.mapper.ProvisionMerchantWebsiteChannelCommandMapper;
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
