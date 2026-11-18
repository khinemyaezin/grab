package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerIssuerClient.Tokens;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcTokenSlots.Slot;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
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
        String subject = principal.getSubject();
        Tokens stored = slots.read(sessionId);
        Tokens tokens = owned(stored, subject);
        if (nearExpiry(tokens)) {
            tokens = slots.locked(sessionId, slot -> refresh(slot, sessionId, subject));
        }
        slots.requireActive(sessionId);
        AuthenticatedActor actor;
        try {
            actor = issuer.actor(tokens);
        } catch (SellerOidcFailure failure) {
            if (!HttpStatus.UNAUTHORIZED.equals(failure.getStatusCode())) {
                throw failure;
            }
            slots.requireActive(sessionId);
            Tokens latestStored = slots.read(sessionId);
            Tokens latest = owned(latestStored, subject);
            if (latest.access().equals(tokens.access())) {
                throw failure;
            }
            actor = issuer.actor(latest);
        }
        slots.requireActive(sessionId);
        return actor;
    }

    public AuthenticatedActor select(HttpServletRequest request, String subject, String assignmentId) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw SellerOidcFailure.invalid();
        }
        String sessionId = session.getId();
        return slots.locked(sessionId, slot -> {
            Tokens current = refresh(slot, sessionId, subject);
            slots.requireActive(sessionId);
            slot.renew();
            Tokens next;
            try {
                next = issuer.select(current, assignmentId);
            } catch (SellerOidcFailure failure) {
                if (!HttpStatus.FORBIDDEN.equals(failure.getStatusCode())) {
                    abandon(slot, current);
                }
                throw failure;
            }
            persistRotation(slot, next);
            slots.requireActive(sessionId);
            AuthenticatedActor actor;
            try {
                slot.renew();
                actor = issuer.actor(next);
            } catch (SellerOidcFailure failure) {
                abandon(slot, next);
                throw failure;
            }
            slots.requireActive(sessionId);
            return actor;
        });
    }

    public URI logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return properties.postLogout();
        }
        String sessionId = session.getId();
        try {
            Tokens tokens = slots.read(sessionId);
            pending.enqueueLogout(sessionId);
            return tokens == null ? properties.postLogout() : issuerLogout(tokens);
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
        slots.requireActive(sessionId);
        Tokens stored = slot.read();
        Tokens existing = owned(stored, subject);
        if (!nearExpiry(existing)) {
            return existing;
        }
        slot.renew();
        Tokens refreshed;
        try {
            refreshed = issuer.refresh(existing);
        } catch (SellerOidcFailure failure) {
            abandon(slot, existing);
            throw failure;
        }
        persistRotation(slot, refreshed);
        return refreshed;
    }

    private void persistRotation(Slot slot, Tokens tokens) {
        try {
            slot.write(tokens);
        } catch (SellerOidcFailure | DataAccessException failure) {
            String refreshToken = tokens.refresh();
            pending.enqueueToken(refreshToken);
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

    private void abandon(Slot slot, Tokens tokens) {
        String refreshToken = tokens.refresh();
        try {
            slot.renew();
            issuer.revoke(refreshToken);
        } catch (SellerOidcFailure failure) {
            pending.enqueueToken(refreshToken);
        }
        slot.write(null);
    }
}
