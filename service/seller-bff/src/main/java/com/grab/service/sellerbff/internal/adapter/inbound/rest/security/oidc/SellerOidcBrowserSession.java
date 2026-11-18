package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOAuthClient.Tokens;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcTokenSlots.Slot;
import com.grab.service.sellerbff.port.outbound.SellerAccessOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerAccessOutboundPort.AccessState;
import com.grab.service.sellerbff.port.outbound.SellerAccessOutboundPort.Intent;
import com.grab.service.sellerbff.port.outbound.SellerAccessOutboundPort.Status;
import com.grab.service.sellerbff.port.inbound.SellerSessionPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;

public final class SellerOidcBrowserSession implements SellerSessionPort {
    private static final Logger LOG = Loggers.getLogger(SellerOidcBrowserSession.class);
    private final SellerOAuthClient oauth;
    private final SellerAccessOutboundPort account;
    private final SellerOidcTokenSlots slots;
    private final SellerPendingRevocations pending;
    private final SellerOidcProperties properties;

    SellerOidcBrowserSession(SellerOAuthClient oauth, SellerAccessOutboundPort account,
                             SellerOidcTokenSlots slots, SellerPendingRevocations pending,
                             SellerOidcProperties properties) {
        this.oauth = oauth;
        this.account = account;
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
        String subject = principal.getSubject();
        if (subject == null || subject.isBlank()) {
            throw SellerOidcFailure.invalid();
        }
        Tokens tokens = new Tokens(access.getTokenValue(), refresh.getTokenValue(), access.getExpiresAt(),
                subject, principal.getIdToken().getTokenValue()).withIssuer(properties.issuer());
        AccessState state = resolveLive(tokens, null, Intent.INITIAL);
        Tokens authorized = tokens.withAccessState(state.userId(), state.assignmentId(),
                state.authenticationVersion(), state.merchantRevision(), state.storefrontRevision());
        slots.start(request.getSession().getId(), authorized);
    }

    AuthenticatedActor authenticate(HttpSession session, OidcUser principal) {
        String sessionId = session.getId();
        String subject = principal.getSubject();
        Tokens tokens = owned(slots.read(sessionId), subject);
        if (nearExpiry(tokens)) {
            tokens = slots.locked(sessionId, slot -> refresh(slot, sessionId, subject));
        }
        slots.requireActive(sessionId);
        AuthenticatedActor actor = validateSession(tokens);
        slots.requireActive(sessionId);
        Tokens latest = owned(slots.read(sessionId), subject);
        if (!latest.access().equals(tokens.access()) || !sameAuthorizationSnapshot(latest, tokens)) {
            throw SellerOidcFailure.invalid();
        }
        return actor;
    }

    @Override
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
            validateSession(current);
            AccessState selected = resolveLive(current, assignmentId, Intent.SELECT);
            Tokens updated = current.withAccessState(selected.userId(), selected.assignmentId(),
                    selected.authenticationVersion(), selected.merchantRevision(), selected.storefrontRevision());
            slot.write(updated);
            slots.requireActive(sessionId);
            return actor(current.subject(), selected);
        });
    }

    public URI logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return properties.postLogout();
        }
        String sessionId = session.getId();
        URI destination;
        try {
            Tokens tokens = slots.read(sessionId);
            pending.enqueueLogout(sessionId);
            destination = tokens == null ? properties.postLogout() : issuerLogout(tokens);
        } finally {
            SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();
            Authentication principal = SecurityContextHolder.getContext().getAuthentication();
            logout.logout(request, response, principal);
        }
        return destination;
    }

    @Override
    public void invalidate(HttpSession session) {
        try {
            pending.enqueueLogout(session.getId());
        } catch (DataAccessException exception) {
            LOG.warn("Seller session invalidation could not queue revocation cause={}",
                    exception.getClass().getSimpleName());
        } finally {
            session.invalidate();
        }
    }

    private AuthenticatedActor validateSession(Tokens tokens) {
        AccessState state = resolveLive(tokens, tokens.assignmentId(), Intent.REVALIDATE);
        if (!properties.issuer().equals(tokens.issuer()) || tokens.platformUserId() == null
                || !tokens.platformUserId().equals(state.userId())
                || tokens.authenticationVersion() != state.authenticationVersion()
                || !same(tokens.assignmentId(), state.assignmentId())
                || tokens.merchantRevision() != state.merchantRevision()
                || tokens.storefrontRevision() != state.storefrontRevision()) {
            LOG.warn("Seller session rejected stage=authorization-snapshot");
            throw SellerOidcFailure.invalid();
        }
        return actor(tokens.subject(), state);
    }

    private AccessState resolveLive(Tokens tokens, String assignmentId, Intent intent) {
        OAuth2AuthenticatedPrincipal introspected = oauth.introspect(tokens.access());
        if (!tokens.subject().equals(introspected.getName())) {
            LOG.warn("Seller session rejected stage=introspection-subject");
            throw SellerOidcFailure.invalid();
        }
        AccessState state = account.resolve(properties.issuer(), tokens.subject(), assignmentId, intent);
        if (state == null) {
            throw SellerOidcFailure.unavailable();
        }
        if (state.status() == Status.UNKNOWN_IDENTITY || state.status() == Status.INACTIVE_IDENTITY) {
            LOG.warn("Seller session rejected stage=account-identity status={}", state.status());
            throw SellerOidcFailure.invalid();
        }
        if (state.status() == Status.DENIED || state.status() == Status.INVALID_CONTEXT) {
            throw SellerOidcFailure.denied();
        }
        if (state.userId() == null || state.email() == null) {
            throw SellerOidcFailure.unavailable();
        }
        return state;
    }

    private URI issuerLogout(Tokens tokens) {
        String endpoint = oauth.logoutUri();
        if (endpoint == null || endpoint.isBlank()) {
            throw SellerOidcFailure.unavailable();
        }
        return UriComponentsBuilder.fromUriString(endpoint).queryParam("id_token_hint", tokens.idToken())
                .queryParam("post_logout_redirect_uri", properties.postLogout()).build().encode().toUri();
    }

    private Tokens refresh(Slot slot, String sessionId, String subject) {
        slots.requireActive(sessionId);
        Tokens stored = owned(slot.read(), subject);
        if (!nearExpiry(stored)) {
            return stored;
        }
        slot.renew();
        Tokens refreshed;
        try {
            refreshed = oauth.refresh(stored);
        } catch (SellerOidcFailure failure) {
            abandon(slot, stored, sessionId);
            throw failure;
        }
        try {
            OAuth2AuthenticatedPrincipal introspected = oauth.introspect(refreshed.access());
            if (!subject.equals(introspected.getName())) {
                throw SellerOidcFailure.invalid();
            }
        } catch (SellerOidcFailure failure) {
            retireUnpersistedRotation(slot, sessionId, refreshed);
            throw failure;
        }
        persistRotation(slot, sessionId, refreshed);
        return refreshed;
    }

    private void retireUnpersistedRotation(Slot slot, String sessionId, Tokens refreshed) {
        slots.markLoggedOut(sessionId);
        pending.enqueueToken(refreshed.refresh());
        slot.write(null);
    }

    private void persistRotation(Slot slot, String sessionId, Tokens tokens) {
        try {
            slot.write(tokens);
        } catch (SellerOidcFailure | DataAccessException failure) {
            pending.enqueueToken(tokens.refresh());
            slots.markLoggedOut(sessionId);
            throw failure;
        }
    }

    private boolean nearExpiry(Tokens tokens) {
        return !Instant.now().plus(properties.refreshSkew()).isBefore(tokens.expiry());
    }

    private Tokens owned(Tokens tokens, String subject) {
        if (tokens == null) {
            LOG.warn("Seller session rejected stage=token-slot-missing");
            throw SellerOidcFailure.invalid();
        }
        if (!subject.equals(tokens.subject())) {
            LOG.warn("Seller session rejected stage=token-owner-mismatch");
            throw SellerOidcFailure.invalid();
        }
        return tokens;
    }

    private void abandon(Slot slot, Tokens tokens, String sessionId) {
        slots.markLoggedOut(sessionId);
        try {
            slot.renew();
            oauth.revoke(tokens.refresh());
        } catch (SellerOidcFailure failure) {
            pending.enqueueToken(tokens.refresh());
        }
        slot.write(null);
    }

    private AuthenticatedActor actor(String subject, AccessState state) {
        AccessContext context = state.assignmentId() == null ? null
                : new AccessContext(state.assignmentId(), state.scopeKey(), state.scopeId());
        return new AuthenticatedActor(state.userId(), properties.issuer(), subject, state.email(), state.roles(),
                state.authorities(), context, state.authenticationVersion());
    }

    private boolean sameAuthorizationSnapshot(Tokens left, Tokens right) {
        return left.platformUserId().equals(right.platformUserId())
                && left.authenticationVersion() == right.authenticationVersion()
                && same(left.assignmentId(), right.assignmentId())
                && left.merchantRevision() == right.merchantRevision()
                && left.storefrontRevision() == right.storefrontRevision();
    }

    private boolean same(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }
}
