package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.AccountUnavailableException;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.Subject;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountFormAuthenticationProvider implements AuthenticationProvider {
    private static final String LOGIN_VERSION = "ACCOUNT_LOGIN_VERSION_";
    private static final String LEGACY_LOGIN_VERSION = "SELLER_LOGIN_VERSION_";

    private final AccountIdentityPort account;
    private final MeterRegistry metrics;

    public AccountFormAuthenticationProvider(AccountIdentityPort account, MeterRegistry metrics) {
        this.account = account;
        this.metrics = metrics;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        try {
            String email = authentication.getName();
            String password = (String) authentication.getCredentials();
            Subject result = account.verify(email, password);
            if (result == null) {
                metrics.counter("authorization.oidc.login.denied").increment();
                throw new BadCredentialsException("Invalid email or password");
            }
            String version = LOGIN_VERSION + result.authenticationVersion();
            List<SimpleGrantedAuthority> attributes = List.of(new SimpleGrantedAuthority(version));
            metrics.counter("authorization.oidc.login.succeeded").increment();
            return UsernamePasswordAuthenticationToken.authenticated(result.userId(), null, attributes);
        } catch (AccountUnavailableException exception) {
            metrics.counter("authorization.oidc.account.unavailable").increment();
            throw new AuthenticationServiceException("Sign-in is temporarily unavailable", exception);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public static long loginVersion(Authentication principal) {
        if (principal != null) {
            for (GrantedAuthority authority : principal.getAuthorities()) {
                String value = authority.getAuthority();
                if (value.startsWith(LOGIN_VERSION)) {
                    return parseVersion(value, LOGIN_VERSION);
                }
                if (value.startsWith(LEGACY_LOGIN_VERSION)) {
                    return parseVersion(value, LEGACY_LOGIN_VERSION);
                }
            }
        }
        return -1;
    }

    private static long parseVersion(String value, String prefix) {
        try {
            String version = value.substring(prefix.length());
            return Long.parseLong(version);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }
}
