package com.grab.framework.outbox;

public record ClaimedOutboxEvent<ID>(
        ID id,
        String claimToken
) {
}
