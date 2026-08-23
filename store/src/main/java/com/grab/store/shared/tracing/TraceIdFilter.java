package com.grab.store.shared.tracing;

import com.grab.framework.logger.slf4j.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String incoming = firstNonBlank(
                request.getHeader(TRACE_ID_HEADER),
                request.getHeader(REQUEST_ID_HEADER)
        );
        String traceId = incoming != null ? incoming : TraceContext.generate();
        response.setHeader(TRACE_ID_HEADER, traceId);

        String previous = TraceContext.current();
        TraceContext.put(traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (previous == null || previous.isBlank()) {
                TraceContext.clear();
            } else {
                TraceContext.put(previous);
            }
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }
}
