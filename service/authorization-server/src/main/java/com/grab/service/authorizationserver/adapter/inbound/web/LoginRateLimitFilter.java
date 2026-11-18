package com.grab.service.authorizationserver.adapter.inbound.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class LoginRateLimitFilter extends OncePerRequestFilter {
    private final StringRedisTemplate redis;
    private final int maxAttempts;
    private final int windowSeconds;
    private final DefaultRedisScript<Long> incrementScript;

    public LoginRateLimitFilter(StringRedisTemplate redis, int maxAttempts, int windowSeconds) {
        this.redis = redis;
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
        this.incrementScript = new DefaultRedisScript<>(String.format("""
                local count = redis.call('INCR', KEYS[1])
                if count == 1 then redis.call('EXPIRE', KEYS[1], %d) end
                return count
                """, windowSeconds), Long.class);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if ("/login".equals(request.getServletPath()) && "POST".equals(request.getMethod())) {
            String clientIp = resolveClientIp(request);
            String key = "grab:issuer:login:" + clientIp;
            Long count;
            try {
                count = redis.execute(incrementScript, List.of(key));
            } catch (DataAccessException exception) {
                response.sendError(503);
                return;
            }
            if (count == null || count > maxAttempts) {
                response.setHeader("Retry-After", String.valueOf(windowSeconds));
                response.sendError(429);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
