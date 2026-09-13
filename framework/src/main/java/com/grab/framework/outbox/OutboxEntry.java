package com.grab.framework.outbox;

import java.time.Duration;
import java.time.LocalDateTime;

public interface OutboxEntry<ID> {
    ID getId();

    String getEventType();

    String getPayload();

    int getEventVersion();

    String getHeaders();

    OutboxStatus getStatus();

    String getClaimToken();

    LocalDateTime getAvailableAt();

    void markProcessing(LocalDateTime now, String claimToken);

    void markPublished(LocalDateTime now);

    void markFailed(LocalDateTime now, String error, Duration retryDelay);
}
