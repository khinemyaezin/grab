package com.grab.store.shared.exception;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExceptionTestController.class)
@Import({GlobalApiExceptionHandler.class, SpringMessageResolver.class})
class GlobalApiExceptionHandlerIntegrationTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Test
    void serviceNotFound_returns404ProblemDetailWithInventoryModule() throws Exception {
        mockMvc.perform(get("/test/errors/service-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("inv.service.location.not_found"))
                .andExpect(jsonPath("$.module").value("inventory"))
                .andExpect(jsonPath("$.args.locationId").value("loc-1"))
                .andExpect(jsonPath("$.retryable").value(false))
                .andExpect(jsonPath("$.path").value("/test/errors/service-not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void domainBusinessRule_returns422ProblemDetail() throws Exception {
        mockMvc.perform(get("/test/errors/domain-insufficient"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("inv.domain.insufficient_quantity"))
                .andExpect(jsonPath("$.args.available").value(3))
                .andExpect(jsonPath("$.args.requested").value(9))
                .andExpect(jsonPath("$.module").value("inventory"));
    }

    @Test
    void validationFailure_returns400ProblemDetail() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("shr.request.validation"))
                .andExpect(jsonPath("$.module").value("shared"))
                .andExpect(jsonPath("$.retryable").value(false));
    }

    @Test
    void constraintViolation_returns400ProblemDetail() throws Exception {
        mockMvc.perform(get("/test/errors/constraint?page=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("shr.request.validation"));
    }

    @Test
    void malformedJson_returns400ProblemDetail() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("shr.request.malformed_json"));
    }

    @Test
    void unexpectedException_returns500ProblemDetail() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("shr.internal.unexpected"))
                .andExpect(jsonPath("$.module").value("shared"))
                .andExpect(jsonPath("$.retryable").value(true));
    }

    @Test
    void inventoryInfrastructureInternal_returnsRetryableWithRetryAfter() throws Exception {
        mockMvc.perform(get("/test/errors/infra-internal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("inv.infra.persistence.internal"))
                .andExpect(jsonPath("$.module").value("inventory"))
                .andExpect(jsonPath("$.retryable").value(true))
                .andExpect(jsonPath("$.retryAfterMs").value(2000));
    }

    @Test
    void traceIdFromMdc_isExposed() throws Exception {
        MDC.put("traceId", "trace-123");
        try {
            mockMvc.perform(get("/test/errors/service-not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.traceId").value("trace-123"));
        } finally {
            MDC.remove("traceId");
        }
    }
}
