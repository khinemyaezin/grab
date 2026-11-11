package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.exception.MessageSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class ProblemDetailAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ProblemDetailAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        MessageSource errorSource = ex instanceof SellerAuthenticationException sellerEx
                ? sellerEx.getMessageSource()
                : new SellerSecurityError.MissingToken();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage() == null ? "Authentication required" : ex.getMessage()
        );
        problem.setTitle("Unauthorized");
        problem.setProperty("code", errorSource.code());
        problem.setProperty("args", errorSource.args());
        problem.setProperty("traceId", MDC.get("traceId"));
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("module", "seller");
        problem.setProperty("retryable", false);

        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}

