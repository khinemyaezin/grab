package com.grab.framework.outbox;

import java.time.Duration;
import java.time.LocalDateTime;

public interface OutboxEntry<ID> {
    ID getId();

    String getEventType();

    String getPayload();

    OutboxStatus getStatus();

    String getClaimToken();

    void markProcessing(LocalDateTime now, String claimToken);

    void markPublished(LocalDateTime now);

    void markFailed(LocalDateTime now, String error, Duration retryDelay);
}
