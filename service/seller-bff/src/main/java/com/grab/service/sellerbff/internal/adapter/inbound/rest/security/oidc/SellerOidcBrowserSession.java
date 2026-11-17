package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerIssuerClient.Tokens;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcTokenSlots.Slot;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;

public final class SellerOidcBrowserSession {
    private final SellerIssuerClient issuer;
    private final SellerOidcTokenSlots slots;
    private final SellerPendingRevocations pending;
    private final SellerOidcProperties properties;

    SellerOidcBrowserSession(SellerIssuerClient issuer, SellerOidcTokenSlots slots, SellerPendingRevocations pending,
                            SellerOidcProperties properties) {
        this.issuer = issuer;
        this.slots = slots;
        this.pending = pending;
        this.properties = properties;
    }

    void start(HttpServletRequest request, OAuth2AuthorizedClient client, OidcUser principal) {
        OAuth2AccessToken access = client.getAccessToken();
        OAuth2RefreshToken refresh = client.getRefreshToken();
        if (refresh == null || access.getExpiresAt() == null) {
            throw SellerOidcFailure.unavailable();
        }
        String sessionId = request.getSession().getId();
        Tokens tokens = new Tokens(access.getTokenValue(), refresh.getTokenValue(), access.getExpiresAt(),
                principal.getSubject(), principal.getIdToken().getTokenValue());
        slots.start(sessionId, tokens);
    }

    AuthenticatedActor authenticate(HttpSession session, OidcUser principal) {
        String sessionId = session.getId();
        Tokens tokens = owned(slots.read(sessionId), principal.getSubject());
        if (nearExpiry(tokens)) {
            tokens = slots.locked(sessionId, slot -> refresh(slot, sessionId, principal.getSubject()));
        }
        try {
            return issuer.actor(tokens);
        } catch (SellerOidcFailure failure) {
            if (!HttpStatus.UNAUTHORIZED.equals(failure.getStatusCode())) {
                throw failure;
            }
            Tokens latest = owned(slots.read(sessionId), principal.getSubject());
            if (latest.access().equals(tokens.access())) {
                throw failure;
            }
            return issuer.actor(latest);
        }
    }

    public AuthenticatedActor select(HttpServletRequest request, String subject, String assignmentId) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw SellerOidcFailure.invalid();
        }
        String sessionId = session.getId();
        return slots.locked(sessionId, slot -> {
            Tokens current = refresh(slot, sessionId, subject);
            Tokens next;
            try {
                next = issuer.select(current, assignmentId);
            } catch (SellerOidcFailure failure) {
                if (!HttpStatus.FORBIDDEN.equals(failure.getStatusCode())) {
                    abandon(slot, sessionId, current);
                }
                throw failure;
            }
            try {
                AuthenticatedActor actor = issuer.actor(next);
                slot.write(next);
                return actor;
            } catch (SellerOidcFailure failure) {
                abandon(slot, sessionId, next);
                throw failure;
            }
        });
    }

    public URI logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return properties.postLogout();
        }
        String sessionId = session.getId();
        try {
            return slots.locked(sessionId, slot -> {
                Tokens tokens = slot.read();
                if (tokens == null) {
                    return properties.postLogout();
                }
                revokeOrQueue(sessionId, tokens.refresh());
                slot.write(null);
                return issuerLogout(tokens);
            });
        } catch (SellerOidcFailure failure) {
            if (HttpStatus.CONFLICT.equals(failure.getStatusCode())) {
                Tokens tokens = slots.read(sessionId);
                if (tokens != null) {
                    pending.enqueue(sessionId, tokens.refresh());
                    return issuerLogout(tokens);
                }
            }
            return properties.postLogout();
        } finally {
            SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();
            Authentication principal = SecurityContextHolder.getContext().getAuthentication();
            logout.logout(request, response, principal);
        }
    }

    private URI issuerLogout(Tokens tokens) {
        return UriComponentsBuilder.fromUriString(properties.issuer())
                .path("/connect/logout").queryParam("id_token_hint", tokens.idToken())
                .queryParam("post_logout_redirect_uri", properties.postLogout())
                .build().encode().toUri();
    }

    private Tokens refresh(Slot slot, String sessionId, String subject) {
        Tokens existing = owned(slot.read(), subject);
        if (!nearExpiry(existing)) {
            return existing;
        }
        try {
            Tokens refreshed = issuer.refresh(existing);
            slot.write(refreshed);
            return refreshed;
        } catch (SellerOidcFailure failure) {
            abandon(slot, sessionId, existing);
            throw failure;
        }
    }

    private boolean nearExpiry(Tokens tokens) {
        Instant threshold = Instant.now().plusSeconds(5);
        return !threshold.isBefore(tokens.expiry());
    }

    private Tokens owned(Tokens tokens, String subject) {
        if (tokens == null || !subject.equals(tokens.subject())) {
            throw SellerOidcFailure.invalid();
        }
        return tokens;
    }

    private void abandon(Slot slot, String sessionId, Tokens tokens) {
        try {
            revokeOrQueue(sessionId, tokens.refresh());
        } finally {
            slot.write(null);
        }
    }

    private void revokeOrQueue(String sessionId, String refresh) {
        try {
            issuer.revoke(refresh);
        } catch (SellerOidcFailure unavailable) {
            pending.enqueue(sessionId, refresh);
        }
    }
}
