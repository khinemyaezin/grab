package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.AccountUnavailableException;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.Subject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LiveIssuerSessionFilter extends OncePerRequestFilter {
    private final AccountIdentityPort account;

    public LiveIssuerSessionFilter(AccountIdentityPort account) {
        this.account = account;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if ("/oauth2/authorize".equals(path) && existing != null && existing.isAuthenticated()) {
            try {
                String subject = existing.getName();
                Subject state = account.resolve(subject);
                if (state == null || state.authenticationVersion() != AccountFormAuthenticationProvider.loginVersion(existing)) {
                    SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();
                    logout.logout(request, response, existing);
                }
            } catch (AccountUnavailableException exception) {
                response.sendError(503);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
