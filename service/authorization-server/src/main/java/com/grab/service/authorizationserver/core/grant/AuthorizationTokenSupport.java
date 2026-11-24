package com.grab.service.authorizationserver.core.grant;

import com.grab.service.authorizationserver.config.AuthorizationClientPolicies;
import com.grab.service.authorizationserver.port.outbound.GrantFamilyStorePort;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.List;
import java.util.Map;

public final class AuthorizationTokenSupport {
    public static final String VERSION = "authentication_version";

    private AuthorizationTokenSupport() {}

    public static OAuth2ClientAuthenticationToken validateClient(Object supplied, AuthorizationClientPolicies clients) {
        if (!(supplied instanceof OAuth2ClientAuthenticationToken client) || !client.isAuthenticated()
                || client.getRegisteredClient() == null
                || !clients.isActive(client.getRegisteredClient().getClientId())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        return client;
    }

    public static boolean owns(OAuth2Authorization authorization, OAuth2ClientAuthenticationToken client) {
        RegisteredClient registration = client.getRegisteredClient();
        return authorization != null && registration.getId().equals(authorization.getRegisteredClientId());
    }

    public static boolean hasAudience(OAuth2Authorization authorization, String audience) {
        if (authorization.getAccessToken() == null) {
            return false;
        }
        Map<String, Object> claims = authorization.getAccessToken().getClaims();
        List<String> expected = List.of(audience);
        return claims != null && expected.equals(claims.get(JwtClaimNames.AUD));
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
