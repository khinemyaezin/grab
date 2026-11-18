package com.grab.service.authorizationserver.core.grant;

import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort.AccountUnavailableException;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Objects;

public final class SellerTokenSupport {
    public static final String VERSION = "authentication_version";
    public static final String ASSIGNMENT = "assignment_id";
    public static final String SCOPE_KEY = "scope_key";
    public static final String SCOPE_ID = "scope_id";
    public static final String MERCHANT_REVISION = "merchant_revision";
    public static final String STOREFRONT_REVISION = "storefront_revision";

    private SellerTokenSupport() {}

    public static OAuth2ClientAuthenticationToken validateClient(Object supplied, String expectedClientId) {
        if (!(supplied instanceof OAuth2ClientAuthenticationToken client) || !client.isAuthenticated()
                || client.getRegisteredClient() == null
                || !expectedClientId.equals(client.getRegisteredClient().getClientId())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        return client;
    }

    public static boolean owns(OAuth2Authorization authorization, OAuth2ClientAuthenticationToken client) {
        RegisteredClient registration = client.getRegisteredClient();
        return authorization != null && registration.getId().equals(authorization.getRegisteredClientId());
    }

    public static long version(OAuth2Authorization authorization, String key) {
        Number version = authorization.getAttribute(key);
        return version == null ? -1 : version.longValue();
    }

    public static AuthenticationState resolve(AccountIdentityPort account, MeterRegistry metrics,
                                              String subject, String assignment, boolean initial) {
        try {
            return account.resolve(subject, assignment, initial);
        } catch (AccountUnavailableException exception) {
            metrics.counter("seller.oidc.account.unavailable").increment();
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }
    }

    public static AuthenticationState liveState(OAuth2Authorization authorization, AccountIdentityPort account,
                                                MeterRegistry metrics) {
        String assignment = authorization.getAttribute(ASSIGNMENT);
        AuthenticationState state = resolve(account, metrics, authorization.getPrincipalName(), assignment, false);
        if (state == null || state.authenticationVersion() != version(authorization, VERSION)
                || state.merchantRevision() != version(authorization, MERCHANT_REVISION)
                || state.storefrontRevision() != version(authorization, STOREFRONT_REVISION)
                || !Objects.equals(authorization.getAttribute(SCOPE_KEY), state.scopeKey())
                || !Objects.equals(authorization.getAttribute(SCOPE_ID), state.scopeId())) {
            return null;
        }
        return state;
    }

    public static OAuth2Authorization withState(OAuth2Authorization authorization, AuthenticationState state) {
        return OAuth2Authorization.from(authorization)
                .attribute(VERSION, state.authenticationVersion())
                .attribute(MERCHANT_REVISION, state.merchantRevision())
                .attribute(STOREFRONT_REVISION, state.storefrontRevision())
                .attributes(attributes -> {
                    attributes.remove(ASSIGNMENT);
                    attributes.remove(SCOPE_KEY);
                    attributes.remove(SCOPE_ID);
                    if (state.assignmentId() != null) {
                        attributes.put(ASSIGNMENT, state.assignmentId());
                        attributes.put(SCOPE_KEY, state.scopeKey());
                        attributes.put(SCOPE_ID, state.scopeId());
                    }
                }).build();
    }

    public static OAuth2Authorization lockCurrent(OAuth2AuthorizationService authorizations,
                                                   GrantFamilyStorePort store,
                                                   String token, OAuth2TokenType type,
                                                   OAuth2ClientAuthenticationToken client) {
        OAuth2Authorization candidate = authorizations.findByToken(token, type);
        if (!owns(candidate, client) || !store.lock(candidate.getId())) {
            return null;
        }
        OAuth2Authorization locked = authorizations.findById(candidate.getId());
        if (!owns(locked, client) || locked.getToken(token) == null) {
            return null;
        }
        return locked;
    }

    public static OAuth2Authorization lockConsumedFamily(OAuth2AuthorizationService authorizations,
                                                         GrantFamilyStorePort store,
                                                         String token,
                                                         OAuth2ClientAuthenticationToken client) {
        String family = store.findConsumedFamily(token);
        if (family == null || !store.lock(family)) {
            return null;
        }
        OAuth2Authorization current = authorizations.findById(family);
        return owns(current, client) ? current : null;
    }

    public static void invalidate(OAuth2AuthorizationService authorizations, OAuth2Authorization authorization) {
        OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
        if (authorization.getAccessToken() != null) {
            builder.invalidate(authorization.getAccessToken().getToken());
        }
        if (authorization.getRefreshToken() != null) {
            builder.invalidate(authorization.getRefreshToken().getToken());
        }
        OAuth2Authorization revoked = builder.build();
        authorizations.save(revoked);
    }
}
