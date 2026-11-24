package com.grab.service.authorizationserver.config.tracing;

import com.grab.framework.logger.slf4j.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                .build();
    }

    @AfterEach
    void clearMdc() {
        TraceContext.clear();
    }

    @Test
    void inboundTraceId_isReusedAndEchoed() throws Exception {
        mockMvc.perform(get("/trace-probe").header(TraceIdFilter.TRACE_ID_HEADER, "auth-trace-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, "auth-trace-123"))
                .andExpect(jsonPath("$.traceId").value("auth-trace-123"));

        assertNull(TraceContext.current());
    }

    @Test
    void inboundRequestId_isUsedWhenTraceIdMissing() throws Exception {
        mockMvc.perform(get("/trace-probe").header(TraceIdFilter.REQUEST_ID_HEADER, "req-trace-456"))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, "req-trace-456"))
                .andExpect(jsonPath("$.traceId").value("req-trace-456"));

        assertNull(TraceContext.current());
    }

    @Test
    void missingHeader_generatesUuid() throws Exception {
        String generated = mockMvc.perform(get("/trace-probe"))
                .andExpect(status().isOk())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andReturn()
                .getResponse()
                .getHeader(TraceIdFilter.TRACE_ID_HEADER);

        assertNotNull(generated);
        assertTrue(!generated.isBlank());
        UUID.fromString(generated);
        assertNull(TraceContext.current());
    }

    @RestController
    static class TraceProbeController {
        @GetMapping("/trace-probe")
        Map<String, String> probe() {
            return Map.of("traceId", String.valueOf(TraceContext.current()));
        }
    }
}
