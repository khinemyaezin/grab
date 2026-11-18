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
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

final class SellerOidcBrowserFilter extends OncePerRequestFilter {
    private static final PathPatternRequestMatcher.Builder PATH = PathPatternRequestMatcher.withDefaults();
    private static final RequestMatcher SELLER_API = PATH.matcher("/api/v1/**");
    private static final RequestMatcher SKIP_LIVE_SELLER = new OrRequestMatcher(
            PATH.matcher(GET, "/api/v1"),
            PATH.matcher(GET, "/api/v1/identity"),
            PATH.matcher(GET, "/api/v1/identity/auth/csrf"),
            PATH.matcher(POST, "/api/v1/identity/auth/register"),
            PATH.matcher(POST, "/api/v1/identity/auth/logout"));

    private final SellerOidcBrowserSession sessions;
    private final ObjectMapper json;
    private final AuthenticationEntryPoint entryPoint;

    SellerOidcBrowserFilter(SellerOidcBrowserSession sessions, ObjectMapper json, AuthenticationEntryPoint entryPoint) {
        this.sessions = sessions;
        this.json = json;
        this.entryPoint = entryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !SELLER_API.matches(request) || SKIP_LIVE_SELLER.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        SecurityContext original = SecurityContextHolder.getContext();
        Authentication authentication = original.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof OidcUser oidc)) {
            entryPoint.commence(request, response, new InsufficientAuthenticationException("Live seller identity required"));
            return;
        }

        HttpSession session = request.getSession(false);
        AuthenticatedActor actor;
        try {
            if (session == null) {
                throw SellerOidcFailure.invalid();
            }
            actor = sessions.authenticate(session, oidc);
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
