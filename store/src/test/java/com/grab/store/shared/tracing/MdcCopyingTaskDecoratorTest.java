package com.grab.store.shared.tracing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MdcCopyingTaskDecoratorTest {

    private final MdcCopyingTaskDecorator decorator = new MdcCopyingTaskDecorator();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void decorate_copiesCallerMdcOntoWorkerAndRestoresAfterward() {
        MDC.put("traceId", "caller-trace");
        AtomicReference<String> observedOnWorker = new AtomicReference<>();

        Runnable decorated = decorator.decorate(() -> observedOnWorker.set(MDC.get("traceId")));

        MDC.put("traceId", "worker-previous");
        decorated.run();

        assertEquals("caller-trace", observedOnWorker.get());
        assertEquals("worker-previous", MDC.get("traceId"));
    }

    @Test
    void decorate_clearsWorkerWhenCallerHadNoMdc() {
        AtomicReference<String> observedOnWorker = new AtomicReference<>("unset");

        Runnable decorated = decorator.decorate(() -> observedOnWorker.set(MDC.get("traceId")));

        MDC.put("traceId", "worker-previous");
        decorated.run();

        assertNull(observedOnWorker.get());
        assertEquals("worker-previous", MDC.get("traceId"));
    }
}
