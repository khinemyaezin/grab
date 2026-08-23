package com.grab.store.shared.tracing;

import com.grab.framework.logger.slf4j.TraceContext;
import com.grab.store.shared.exception.GlobalApiExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TraceIdFilterTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TraceProbeController())
                .addFilters(new TraceIdFilter())
                .setControllerAdvice(new GlobalApiExceptionHandler(new EmptyObjectProvider<>()))
                .build();
    }

    @AfterEach
    void clearMdc() {
        TraceContext.clear();
    }

    @Test
    void inboundTraceId_isReusedAndEchoed() throws Exception {
        mockMvc.perform(get("/trace-probe").header(TraceIdFilter.TRACE_ID_HEADER, "inbound-trace"))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, "inbound-trace"))
                .andExpect(jsonPath("$.traceId").value("inbound-trace"));

        assertNull(TraceContext.current());
    }

    @Test
    void inboundRequestId_isUsedWhenTraceIdMissing() throws Exception {
        mockMvc.perform(get("/trace-probe").header(TraceIdFilter.REQUEST_ID_HEADER, "request-trace"))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, "request-trace"))
                .andExpect(jsonPath("$.traceId").value("request-trace"));
    }

    @Test
    void missingHeader_generatesUuid() throws Exception {
        String generated = mockMvc.perform(get("/trace-probe"))
                .andExpect(status().isOk())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andReturn()
                .getResponse()
                .getHeader(TraceIdFilter.TRACE_ID_HEADER);

        assertTrue(generated != null && !generated.isBlank());
        java.util.UUID.fromString(generated);
        assertNull(TraceContext.current());
    }

    @Test
    void problemDetail_includesGeneratedTraceId() throws Exception {
        mockMvc.perform(get("/trace-error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @RestController
    static class TraceProbeController {
        @GetMapping("/trace-probe")
        Map<String, String> probe() {
            return Map.of("traceId", String.valueOf(TraceContext.current()));
        }

        @GetMapping("/trace-error")
        void error() {
            throw new RuntimeException("boom");
        }
    }

    private static final class EmptyObjectProvider<T> implements ObjectProvider<T> {
        @Override
        public T getObject(Object... args) throws BeansException {
            return null;
        }

        @Override
        public T getObject() throws BeansException {
            return null;
        }

        @Override
        public T getIfAvailable() {
            return null;
        }

        @Override
        public T getIfUnique() {
            return null;
        }

        @Override
        public Iterator<T> iterator() {
            return java.util.Collections.emptyIterator();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
        }
    }
}
