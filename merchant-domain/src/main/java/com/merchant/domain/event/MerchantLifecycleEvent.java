package com.merchant.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public interface MerchantLifecycleEvent extends Event {
    String eventId();
    String merchantId();
    String applicantUserId();
    String status();
    String actorId();
    long aggregateVersion();
    Instant occurredAt();
}
