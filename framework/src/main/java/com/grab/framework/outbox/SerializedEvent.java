package com.grab.framework.outbox;

public record SerializedEvent(
        String eventType,
        String payload,
        int eventVersion,
        String headers
) {
}
