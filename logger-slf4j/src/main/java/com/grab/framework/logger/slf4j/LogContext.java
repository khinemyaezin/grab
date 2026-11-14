package com.grab.framework.logger.slf4j;

import org.slf4j.MDC;

/**
 * Scoped MDC entries other than {@code traceId}; the previous value is restored afterwards.
 */
public final class LogContext {

    /** Outbox {@code eventId} of the event being delivered to listeners. */
    public static final String EVENT_ID = "eventId";

    private LogContext() {
    }

    public static void run(String key, String value, Runnable action) {
        String previous = MDC.get(key);
        put(key, value);
        try {
            action.run();
        } finally {
            put(key, previous);
        }
    }

    private static void put(String key, String value) {
        if (value == null || value.isBlank()) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, value);
    }
}
