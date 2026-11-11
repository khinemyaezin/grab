package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.filters;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.exception.SellerAuthenticationException;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.exception.SellerSecurityError;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.util.SellerAuthCookieHelper;
import com.grab.framework.security.AccessTokenAuthenticator;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.framework.security.ExternalPrincipal;
import com.grab.framework.security.PlatformIdentityResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ProviderBearerAuthenticationFilter extends OncePerRequestFilter {

    private final AccessTokenAuthenticator authenticator;
    private final ObjectProvider<PlatformIdentityResolver> resolvers;
    private final AuthenticationEntryPoint entryPoint;
    private final SellerAuthCookieHelper authCookieHelper;

    public ProviderBearerAuthenticationFilter(
            AccessTokenAuthenticator authenticator,
            ObjectProvider<PlatformIdentityResolver> resolvers,
            AuthenticationEntryPoint entryPoint,
            SellerAuthCookieHelper authCookieHelper) {
        this.authenticator = authenticator;
        this.resolvers = resolvers;
        this.entryPoint = entryPoint;
        this.authCookieHelper = authCookieHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = null;
        String header = request.getHeader("Authorization");

        if (header != null) {
            if (!header.startsWith("Bearer ") || header.length() <= 7) {
                entryPoint.commence(
                        request,
                        response,
                        new SellerAuthenticationException(new SellerSecurityError.MalformedToken(), "Malformed Bearer token")
                );
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
            PlatformIdentityResolver resolver = resolvers.getIfAvailable();
            if (resolver == null) {
                throw new SellerAuthenticationException(
                        new SellerSecurityError.IdentityNotLinked(),
                        "Identity resolver is unavailable"
                );
            }
            AuthenticatedActor actor = resolver.resolve(external);
            SellerSecurityPrincipal principal = new SellerSecurityPrincipal(actor);
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (SellerAuthenticationException ex) {
            SecurityContextHolder.clearContext();
            authCookieHelper.clearTokenCookies().forEach((headerName, headerValues) -> {
                headerValues.forEach(value -> response.addHeader(headerName, value));
            });
            entryPoint.commence(request, response, ex);
        }
    }
}

