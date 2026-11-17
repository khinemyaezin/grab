package com.grab.service.contract.event.account.merchant;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record MerchantReactivatedIntegrationEvent(
        String merchantId,
        String applicantUserId,
        String merchantName,
        String merchantType,
        String status,
        Instant occurredAt,
        int version
) implements Event {
}
