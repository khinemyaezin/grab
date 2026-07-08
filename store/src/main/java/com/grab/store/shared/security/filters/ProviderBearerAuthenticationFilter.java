package com.grab.store.shared.security.filters;

import com.grab.framework.security.*;
import com.grab.store.shared.security.SecurityPrincipal;
import com.grab.store.shared.security.expection.IdentityAuthenticationException;
import com.grab.store.shared.security.expection.IdentitySecurityError;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import com.grab.store.shared.security.util.AuthCookieHelper;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
public class ProviderBearerAuthenticationFilter extends OncePerRequestFilter {
    private final AccessTokenAuthenticator authenticator;
    private final ObjectProvider<PlatformIdentityResolver> resolvers;
    private final AuthenticationEntryPoint entryPoint;
    private final AuthCookieHelper authCookieHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = null;
        String header = request.getHeader("Authorization");

        if (header != null) {
            if (!header.startsWith("Bearer ") || header.length() <= 7) {
                entryPoint.commence(request, response, new IdentityAuthenticationException(new IdentitySecurityError.MalformedToken(), "Malformed Bearer token"));
                return;
            }
            token = header.substring(7);
        } else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            ExternalPrincipal external = authenticator.authenticate(token);
            AuthenticatedActor actor = Objects.requireNonNull(resolvers.getIfAvailable()).resolve(external);
            SecurityPrincipal principal = new SecurityPrincipal(actor);
            var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (IdentityAuthenticationException ex) {
            SecurityContextHolder.clearContext();
            authCookieHelper.clearTokenCookies()
                    .forEach((headerName, headerValues) -> {
                        headerValues.forEach(value ->
                                response.addHeader(headerName, value));
                    });
            entryPoint.commence(request, response, ex);
        }
    }
}
