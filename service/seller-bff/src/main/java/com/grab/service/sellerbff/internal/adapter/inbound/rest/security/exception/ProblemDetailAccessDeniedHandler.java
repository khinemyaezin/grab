package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ProblemDetailAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        boolean contextRequired = ex instanceof SellerContextRequiredException;
        HttpStatus status = contextRequired ? HttpStatus.CONFLICT : HttpStatus.FORBIDDEN;
        String detail = contextRequired ? ex.getMessage() : "Access denied";
        String code = contextRequired ? "idt.service.auth.access_context_required" : "idt.service.auth.access_denied";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("code", code);
        problem.setProperty("args", Map.of());
        problem.setProperty("traceId", MDC.get("traceId"));
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("module", "seller");
        problem.setProperty("retryable", false);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}

