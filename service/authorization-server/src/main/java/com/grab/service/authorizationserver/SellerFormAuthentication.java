package com.grab.service.authorizationserver;

import com.grab.service.authorizationserver.SellerAccountAuthentication.AccountUnavailable;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.VerifiedUser;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

final class SellerFormAuthentication implements AuthenticationProvider {
    private static final String LOGIN_VERSION = "SELLER_LOGIN_VERSION_";
    private final SellerAccountAuthentication account;
    private final MeterRegistry metrics;

    SellerFormAuthentication(SellerAccountAuthentication account, MeterRegistry metrics) {
        this.account = account;
        this.metrics = metrics;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        try {
            String email = authentication.getName();
            String password = (String) authentication.getCredentials();
            VerifiedUser result = account.verify(email, password);
            if (result == null) {
                metrics.counter("seller.oidc.login.denied").increment();
                throw new BadCredentialsException("Invalid email or password");
            }
            String version = LOGIN_VERSION + result.authenticationVersion();
            List<SimpleGrantedAuthority> attributes = List.of(new SimpleGrantedAuthority(version));
            metrics.counter("seller.oidc.login.succeeded").increment();
            return UsernamePasswordAuthenticationToken.authenticated(result.userId(), null, attributes);
        } catch (AccountUnavailable exception) {
            metrics.counter("seller.oidc.account.unavailable").increment();
            throw new AuthenticationServiceException("Sign-in is temporarily unavailable", exception);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    static long loginVersion(Authentication principal) {
        if (principal != null) {
            for (GrantedAuthority authority : principal.getAuthorities()) {
                String value = authority.getAuthority();
                if (value.startsWith(LOGIN_VERSION)) {
                    return Long.parseLong(value.substring(LOGIN_VERSION.length()));
                }
            }
        }
        return -1;
    }

    static final class LiveIssuerSession extends OncePerRequestFilter {
        private final SellerAccountAuthentication account;

        LiveIssuerSession(SellerAccountAuthentication account) {
            this.account = account;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String path = request.getServletPath();
            Authentication existing = SecurityContextHolder.getContext().getAuthentication();
            if ("/oauth2/authorize".equals(path) && existing != null && existing.isAuthenticated()) {
                try {
                    AuthenticationState state = account.resolve(existing.getName(), null, false);
                    if (state == null || state.authenticationVersion() != loginVersion(existing)) {
                        SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();
                        logout.logout(request, response, existing);
                    }
                } catch (AccountUnavailable exception) {
                    response.sendError(503);
                    return;
                }
            }
            chain.doFilter(request, response);
        }
    }

    static final class LoginRateLimit extends OncePerRequestFilter {
        private static final DefaultRedisScript<Long> INCREMENT = new DefaultRedisScript<>("""
                local count = redis.call('INCR', KEYS[1])
                if count == 1 then redis.call('EXPIRE', KEYS[1], 60) end
                return count
                """, Long.class);
        private final StringRedisTemplate redis;

        LoginRateLimit(StringRedisTemplate redis) {
            this.redis = redis;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            if ("/login".equals(request.getServletPath()) && "POST".equals(request.getMethod())) {
                String key = "grab:issuer:login:" + request.getRemoteAddr();
                Long count;
                try {
                    count = redis.execute(INCREMENT, List.of(key));
                } catch (DataAccessException exception) {
                    response.sendError(503);
                    return;
                }
                if (count == null || count > 30) {
                    response.setHeader("Retry-After", "60");
                    response.sendError(429);
                    return;
                }
            }
            chain.doFilter(request, response);
        }
    }
}
