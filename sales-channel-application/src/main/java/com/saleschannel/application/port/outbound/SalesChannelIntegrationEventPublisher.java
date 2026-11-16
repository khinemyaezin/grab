package com.saleschannel.application.port.outbound;

import com.saleschannel.domain.event.SalesChannelDisabledEvent;
import com.saleschannel.domain.event.SalesChannelEnabledEvent;

public interface SalesChannelIntegrationEventPublisher {

    void publishSalesChannelEnabled(SalesChannelEnabledEvent event);

    void publishSalesChannelDisabled(SalesChannelDisabledEvent event);
}

