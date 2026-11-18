package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.AccountUnavailableException;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.VerifiedUser;
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
public class SellerFormAuthenticationProvider implements AuthenticationProvider {
    public static final String LOGIN_VERSION = "SELLER_LOGIN_VERSION_";

    private final AccountIdentityPort account;
    private final MeterRegistry metrics;

    public SellerFormAuthenticationProvider(AccountIdentityPort account, MeterRegistry metrics) {
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
        } catch (AccountUnavailableException exception) {
            metrics.counter("seller.oidc.account.unavailable").increment();
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
                    return Long.parseLong(value.substring(LOGIN_VERSION.length()));
                }
            }
        }
        return -1;
    }
}
