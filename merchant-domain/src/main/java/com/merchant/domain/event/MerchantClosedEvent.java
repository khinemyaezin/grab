package com.merchant.domain.event;

import java.time.Instant;

public record MerchantClosedEvent(String eventId, String merchantId, String applicantUserId,
                                  String status, String actorId, long aggregateVersion,
                                  Instant occurredAt) implements MerchantLifecycleEvent { }
