package com.grab.app.channel.saleschannel.internal.adapter.inbound.event;

import com.grab.app.channel.saleschannel.internal.adapter.inbound.event.mapper.ProvisionMerchantWebsiteChannelCommandMapper;
import com.grab.app.shared.events.merchant.MerchantApprovedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class MerchantApprovedSalesChannelEventListener extends AbstractIntegrationEventListener {
    private final CommandBus commandBus;
    private final ProvisionMerchantWebsiteChannelCommandMapper mapper;

    public MerchantApprovedSalesChannelEventListener(
            @Qualifier("channelCommandBus") CommandBus commandBus,
            ProvisionMerchantWebsiteChannelCommandMapper mapper,
            @Qualifier("salesChannelTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("salesChannelProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.commandBus = commandBus;
        this.mapper = mapper;
    }

    @EventListener
    public void onMerchantApproved(MerchantApprovedIntegrationEvent event) {
        processDelivery("saleschannel.provision-merchant-website", () -> {
            commandBus.dispatch(mapper.toCommand(event));
        });
    }
}
