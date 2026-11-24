package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AccountFormAuthenticationProvider implements AuthenticationProvider {
    private static final String LOGIN_VERSION = "ACCOUNT_LOGIN_VERSION_";
    private static final String LEGACY_LOGIN_VERSION = "SELLER_LOGIN_VERSION_";

    private final IamUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final MeterRegistry metrics;

    public AccountFormAuthenticationProvider(IamUserRepository users,
                                           PasswordEncoder passwordEncoder,
                                           MeterRegistry metrics) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.metrics = metrics;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String identifier = authentication.getName();
        String password = (String) authentication.getCredentials();

        Optional<IamUser> found = users.findByUsernameOrEmail(identifier);
        if (found.isEmpty()) {
            metrics.counter("authorization.oidc.login.denied").increment();
            throw new BadCredentialsException("Invalid email or password");
        }

        IamUser user = found.get();
        if (!user.enabled()) {
            metrics.counter("authorization.oidc.login.disabled").increment();
            throw new DisabledException("Account is disabled");
        }

        if (password == null || !passwordEncoder.matches(password, user.passwordHash())) {
            metrics.counter("authorization.oidc.login.denied").increment();
            throw new BadCredentialsException("Invalid email or password");
        }

        metrics.counter("authorization.oidc.login.succeeded").increment();
        String version = LOGIN_VERSION + user.authenticationVersion();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(version));
        for (String auth : user.authorities()) {
            authorities.add(new SimpleGrantedAuthority(auth));
        }

        return UsernamePasswordAuthenticationToken.authenticated(user.id(), null, authorities);
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
            return Long.parseLong(value.substring(prefix.length()));
        } catch (NumberFormatException exception) {
            return -1;
        }
    }
}
