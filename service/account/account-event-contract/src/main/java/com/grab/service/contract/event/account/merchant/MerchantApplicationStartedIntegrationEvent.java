package com.grab.service.contract.event.account.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantApplicationStartedIntegrationEvent(
        String merchantId,
        String merchantName,
        long aggregateVersion,
        Instant occurredAt
) implements Event {
}
