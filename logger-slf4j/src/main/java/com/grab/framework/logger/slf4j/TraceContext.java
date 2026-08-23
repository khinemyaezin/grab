package com.grab.framework.logger.slf4j;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.Callable;

public final class TraceContext {

    public static final String MDC_KEY = "traceId";

    private TraceContext() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static void put(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            clear();
            return;
        }
        MDC.put(MDC_KEY, traceId);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }

    public static void run(String traceId, Runnable action) {
        String previous = current();
        put(traceId);
        try {
            action.run();
        } finally {
            restore(previous);
        }
    }

    public static <T> T call(String traceId, Callable<T> action) throws Exception {
        String previous = current();
        put(traceId);
        try {
            return action.call();
        } finally {
            restore(previous);
        }
    }

    public static void runWithGenerated(Runnable action) {
        run(generate(), action);
    }

    public static <T> T callWithGenerated(Callable<T> action) throws Exception {
        return call(generate(), action);
    }

    private static void restore(String previous) {
        if (previous == null || previous.isBlank()) {
            clear();
            return;
        }
        put(previous);
    }
}
