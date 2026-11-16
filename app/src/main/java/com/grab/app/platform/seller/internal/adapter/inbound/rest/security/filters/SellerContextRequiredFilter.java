package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class SellerContextRequiredFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public SellerContextRequiredFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof SellerSecurityPrincipal principal) {

            if (principal.getAccessContext().isEmpty()) {
                String path = request.getRequestURI();
                if (!path.startsWith("/api/v1/identity/auth")
                        && !path.startsWith("/api/v1/identity/session/contexts")
                        && !path.startsWith("/api/v1")
                        && !path.startsWith("/swagger-ui")
                        && !path.startsWith("/v3/api-docs")) {

                    sendContextRequired(request, response);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private void sendContextRequired(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Access context is required");
        problem.setTitle("Conflict");
        problem.setProperty("code", "idt.service.auth.access_context_required");
        problem.setProperty("args", Map.of());
        problem.setProperty("traceId", MDC.get("traceId"));
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("module", "seller");
        problem.setProperty("retryable", false);

        response.setStatus(409);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}

