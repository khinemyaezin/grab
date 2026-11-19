package com.grab.service.authorizationserver.adapter.inbound.web;

import com.grab.service.authorizationserver.core.user.IamUser;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class LiveIssuerSessionFilter extends OncePerRequestFilter {
    private final IamUserRepository users;

    public LiveIssuerSessionFilter(IamUserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if ("/oauth2/authorize".equals(path) && existing != null && existing.isAuthenticated()) {
            String subject = existing.getName();
            Optional<IamUser> user = users.findById(subject);
            if (user.isEmpty() || !user.get().enabled()
                    || user.get().authenticationVersion() != AccountFormAuthenticationProvider.loginVersion(existing)) {
                SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();
                logout.logout(request, response, existing);
            }
        }
        chain.doFilter(request, response);
    }
}
