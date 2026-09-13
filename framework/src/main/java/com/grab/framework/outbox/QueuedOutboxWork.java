package com.grab.framework.outbox;

public record QueuedOutboxWork<ID>(
        ID id,
        OutboxWorkSource source,
        String claimToken
) {
    public static <ID> QueuedOutboxWork<ID> hot(ID id) {
        return new QueuedOutboxWork<>(id, OutboxWorkSource.HOT, null);
    }

    public static <ID> QueuedOutboxWork<ID> cold(ClaimedOutboxEvent<ID> claimed) {
        return new QueuedOutboxWork<>(claimed.id(), OutboxWorkSource.COLD, claimed.claimToken());
    }
}
