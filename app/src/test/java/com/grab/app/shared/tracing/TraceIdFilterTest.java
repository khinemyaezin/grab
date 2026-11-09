package com.grab.app.shared.tracing;

import com.grab.framework.logger.slf4j.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TraceIdFilterTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TraceProbeController controller = new TraceProbeController();
        TraceIdFilter filter = new TraceIdFilter();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(filter)
                .build();
    }

    @AfterEach
    void clearMdc() {
        TraceContext.clear();
    }

    @Test
    void doFilterInternal_whenTraceIdHeaderPresent_shouldReuseAndEchoHeader() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/trace-probe")
                .header(TraceIdFilter.TRACE_ID_HEADER, "inbound-trace");

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, "inbound-trace"))
                .andExpect(jsonPath("$.traceId").value("inbound-trace"));

        String currentTrace = TraceContext.current();
        assertNull(currentTrace);
    }

    @Test
    void doFilterInternal_whenOnlyRequestIdHeaderPresent_shouldUseRequestId() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/trace-probe")
                .header(TraceIdFilter.REQUEST_ID_HEADER, "request-trace");

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, "request-trace"))
                .andExpect(jsonPath("$.traceId").value("request-trace"));

        String currentTrace = TraceContext.current();
        assertNull(currentTrace);
    }

    @Test
    void doFilterInternal_whenHeadersMissing_shouldGenerateUuid() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/trace-probe");

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(header().exists(TraceIdFilter.TRACE_ID_HEADER))
                .andReturn();

        String generated = mvcResult.getResponse().getHeader(TraceIdFilter.TRACE_ID_HEADER);
        assertNotNull(generated);
        boolean isNotBlank = !generated.isBlank();
        assertTrue(isNotBlank);

        UUID parsedUuid = UUID.fromString(generated);
        assertNotNull(parsedUuid);

        String currentTrace = TraceContext.current();
        assertNull(currentTrace);
    }

    @Test
    void doFilterInternal_whenDownstreamThrowsException_shouldClearTraceContext() {
        MockHttpServletRequestBuilder requestBuilder = get("/trace-error");

        assertThrows(Exception.class, () -> mockMvc.perform(requestBuilder));

        String currentTrace = TraceContext.current();
        assertNull(currentTrace);
    }

    @RestController
    static class TraceProbeController {
        @GetMapping("/trace-probe")
        Map<String, String> probe() {
            String current = String.valueOf(TraceContext.current());
            return Map.of("traceId", current);
        }

        @GetMapping("/trace-error")
        void error() {
            throw new IllegalStateException("Simulated controller error");
        }
    }
}

