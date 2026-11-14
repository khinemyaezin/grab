package com.grab.framework.outbox;

/**
 * Raised when an outbox row can never be delivered (unknown event type, unreadable payload,
 * unsupported encoding). The processor moves such rows straight to {@link OutboxStatus#DEAD}
 * instead of retrying them.
 */
public class OutboxPoisonEventException extends IllegalStateException {

    public OutboxPoisonEventException(String message) {
        super(message);
    }

    public OutboxPoisonEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
