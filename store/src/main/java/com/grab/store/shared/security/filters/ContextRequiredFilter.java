package com.grab.store.shared.security.filters;

import com.grab.store.shared.security.SecurityPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ContextRequiredFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof SecurityPrincipal principal) {

            if (principal.getAccessContext().isEmpty()) {
                String path = request.getRequestURI();
                if (!path.startsWith("/api/v1/identity/auth")
                        && !path.startsWith("/api/v1/identity/access-contexts")
                        && !path.startsWith("/api/v1")
                        && !path.startsWith("/swagger-ui")
                        && !path.startsWith("/v3/api-docs")) {

                    sendContextRequired(response);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private void sendContextRequired(HttpServletResponse response) throws IOException {
        response.setStatus(409);
        response.setContentType("application/problem+json");
    }
}