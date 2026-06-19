package com.grab.store.shared.security.expection;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ProblemDetailAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        com.grab.framework.exception.MessageSource errorSource = ex instanceof IdentityAuthenticationException identity
            ? identity.getMessageSource() 
            : new IdentitySecurityError.MissingToken();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage() == null ? "Authentication required" : ex.getMessage());
        problem.setTitle("Unauthorized");
        problem.setProperty("code", errorSource.code());
        problem.setProperty("args", errorSource.args());
        problem.setProperty("traceId", MDC.get("traceId"));
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("module", "identity");
        problem.setProperty("retryable", false);
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
