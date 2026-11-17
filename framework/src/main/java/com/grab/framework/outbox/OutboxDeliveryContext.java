package com.grab.framework.outbox;

import java.util.Optional;
import java.util.UUID;

/**
 * Exposes the outbox {@code eventId} of the event currently being dispatched to synchronous listeners.
 * Listeners copy it into the command they dispatch so the consuming handler can record it in its inbox
 * ({@code processed_event}) and drop redeliveries.
 */
public final class OutboxDeliveryContext {

    private static final ThreadLocal<UUID> CURRENT_EVENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<OutboxDispatchMetadata> CURRENT_METADATA = new ThreadLocal<>();

    private OutboxDeliveryContext() {
    }

    public static Optional<UUID> currentEventId() {
        return Optional.ofNullable(CURRENT_EVENT_ID.get());
    }

    public static Optional<OutboxDispatchMetadata> currentMetadata() {
        return Optional.ofNullable(CURRENT_METADATA.get());
    }

    public static void run(UUID eventId, Runnable action) {
        UUID previous = CURRENT_EVENT_ID.get();
        OutboxDispatchMetadata previousMetadata = CURRENT_METADATA.get();
        CURRENT_METADATA.remove();
        if (eventId == null) {
            CURRENT_EVENT_ID.remove();
        } else {
            CURRENT_EVENT_ID.set(eventId);
        }
        try {
            action.run();
        } finally {
            if (previousMetadata == null) {
                CURRENT_METADATA.remove();
            } else {
                CURRENT_METADATA.set(previousMetadata);
            }
            if (previous == null) {
                CURRENT_EVENT_ID.remove();
            } else {
                CURRENT_EVENT_ID.set(previous);
            }
        }
    }

    public static void run(OutboxDispatchMetadata metadata, Runnable action) {
        UUID previousEventId = CURRENT_EVENT_ID.get();
        OutboxDispatchMetadata previousMetadata = CURRENT_METADATA.get();
        CURRENT_METADATA.set(metadata);
        CURRENT_EVENT_ID.set(metadata.eventId());
        try {
            action.run();
        } finally {
            if (previousMetadata == null) {
                CURRENT_METADATA.remove();
            } else {
                CURRENT_METADATA.set(previousMetadata);
            }
            if (previousEventId == null) {
                CURRENT_EVENT_ID.remove();
            } else {
                CURRENT_EVENT_ID.set(previousEventId);
            }
        }
    }
}
