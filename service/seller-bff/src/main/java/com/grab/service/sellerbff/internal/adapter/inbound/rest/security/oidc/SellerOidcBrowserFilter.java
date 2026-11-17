package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

final class SellerOidcBrowserFilter extends OncePerRequestFilter {
    private final SellerOidcBrowserSession sessions;
    private final ObjectMapper json;

    SellerOidcBrowserFilter(SellerOidcBrowserSession sessions, ObjectMapper json) {
        this.sessions = sessions;
        this.json = json;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/api/v1/") || path.equals("/api/v1/identity")
                || path.startsWith("/api/v1/identity/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        SecurityContext original = SecurityContextHolder.getContext();
        Authentication authentication = original.getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser oidc)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        AuthenticatedActor actor;
        try {
            if (session == null) {
                throw SellerOidcFailure.invalid();
            }
            actor = sessions.authenticate(session, oidc);
            SellerOidcRouteAccess.require(request, actor);
        } catch (SellerOidcFailure failure) {
            if (session != null && HttpStatus.UNAUTHORIZED.equals(failure.getStatusCode())) {
                session.invalidate();
            }
            ProblemDetail problem = failure.getBody();
            response.setStatus(problem.getStatus());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            json.writeValue(response.getOutputStream(), problem);
            return;
        } catch (DataAccessException exception) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        SellerSecurityPrincipal principal = new SellerSecurityPrincipal(actor);
        Authentication live = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        SecurityContext current = SecurityContextHolder.createEmptyContext();
        current.setAuthentication(live);
        SecurityContextHolder.setContext(current);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.setContext(original);
        }
    }
}
