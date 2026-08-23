package com.grab.framework.logger.slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceContextTest {

    @AfterEach
    void clearMdc() {
        TraceContext.clear();
    }

    @Test
    void put_thenCurrent_returnsValue() {
        TraceContext.put("trace-1");

        assertEquals("trace-1", TraceContext.current());
    }

    @Test
    void clear_removesValue() {
        TraceContext.put("trace-1");
        TraceContext.clear();

        assertNull(TraceContext.current());
    }

    @Test
    void generate_returnsUuid() {
        String generated = TraceContext.generate();

        UUID parsed = UUID.fromString(generated);
        assertEquals(generated, parsed.toString());
        assertNotEquals(TraceContext.generate(), generated);
    }

    @Test
    void run_setsValueDuringActionAndRestoresPrevious() {
        TraceContext.put("previous");
        AtomicReference<String> observed = new AtomicReference<>();

        TraceContext.run("nested", () -> observed.set(TraceContext.current()));

        assertEquals("nested", observed.get());
        assertEquals("previous", TraceContext.current());
    }

    @Test
    void run_clearsWhenNoPreviousValue() {
        TraceContext.run("only", () -> assertEquals("only", TraceContext.current()));

        assertNull(TraceContext.current());
    }

    @Test
    void call_returnsResultAndRestores() throws Exception {
        TraceContext.put("previous");

        String result = TraceContext.call("nested", () -> TraceContext.current());

        assertEquals("nested", result);
        assertEquals("previous", TraceContext.current());
    }

    @Test
    void run_whenActionThrows_stillRestores() {
        TraceContext.put("previous");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                TraceContext.run("nested", () -> {
                    throw new IllegalStateException("boom");
                })
        );

        assertEquals("boom", thrown.getMessage());
        assertEquals("previous", TraceContext.current());
    }

    @Test
    void runWithGenerated_usesUuidAndClearsAfterward() {
        AtomicReference<String> observed = new AtomicReference<>();

        TraceContext.runWithGenerated(() -> observed.set(TraceContext.current()));

        assertTrue(observed.get() != null && !observed.get().isBlank());
        UUID.fromString(observed.get());
        assertNull(TraceContext.current());
    }
}
