package com.grab.framework.outbox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxEventHeadersTest {

    @Test
    void mergeTraceId_whenBlank_returnsOriginalHeaders() {
        assertEquals("{}", OutboxEventHeaders.mergeTraceId("{}", null));
        assertEquals("{}", OutboxEventHeaders.mergeTraceId("{}", "  "));
    }

    @Test
    void mergeTraceId_addsTraceIdToJsonObject() {
        String merged = OutboxEventHeaders.mergeTraceId("{\"contentType\":\"application/json\"}", "trace-123");

        assertTrue(merged.contains("\"contentType\":\"application/json\""));
        assertTrue(merged.contains("\"traceId\":\"trace-123\""));
        assertEquals("trace-123", OutboxEventHeaders.extractTraceId(merged));
    }

    @Test
    void extractTraceId_whenMissing_returnsNull() {
        assertNull(OutboxEventHeaders.extractTraceId("{}"));
        assertNull(OutboxEventHeaders.extractTraceId("{\"contentType\":\"application/json\"}"));
        assertNull(OutboxEventHeaders.extractTraceId("not-json"));
    }
}
