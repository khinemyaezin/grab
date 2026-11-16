package com.merchant.application.port.outbound;

import com.merchant.domain.event.MerchantApplicationStartedEvent;
import com.merchant.domain.event.MerchantApprovedEvent;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import com.merchant.domain.event.MerchantReactivatedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontRenamedEvent;
import com.merchant.domain.event.StorefrontStatusChangedEvent;

public interface MerchantIntegrationEventPublisher {

    void publishMerchantApproved(MerchantApprovedEvent event);

    void publishMerchantSuspended(MerchantSuspendedEvent event);

    void publishMerchantReactivated(MerchantReactivatedEvent event);

    void publishMerchantClosed(MerchantClosedEvent event);

    void publishStorefrontCreated(StorefrontCreatedEvent event);

    void publishStorefrontStatusChanged(StorefrontStatusChangedEvent event);

    void publishMerchantApplicationStarted(MerchantApplicationStartedEvent event);

    void publishMerchantProfileUpdated(MerchantProfileUpdatedEvent event);

    void publishStorefrontRenamed(StorefrontRenamedEvent event);
}

