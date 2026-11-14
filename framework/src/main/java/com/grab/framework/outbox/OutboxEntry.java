package com.grab.framework.outbox;

import java.time.Instant;
import java.util.UUID;

public interface OutboxEntry<ID> {
    ID getId();

    UUID getEventId();

    String getEventType();

    String getPayload();

    int getEventVersion();

    String getHeaders();

    OutboxStatus getStatus();

    String getClaimToken();

    int getAttemptCount();

    Instant getOccurredAt();

    Instant getAvailableAt();

    void markProcessing(Instant now, String claimToken);

    void markPublished(Instant now);

    void markFailed(String error, Instant nextAvailableAt);

    void markDead(Instant now, String error);

    void releaseClaim(Instant now);

    default boolean isClaimedBy(String claimToken) {
        return getStatus() == OutboxStatus.PROCESSING
                && claimToken != null
                && claimToken.equals(getClaimToken());
    }
}
