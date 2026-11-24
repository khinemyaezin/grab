package com.grab.service.authorizationserver.core.token;

import com.grab.service.authorizationserver.config.AuthorizationClientPolicies;
import com.grab.service.authorizationserver.core.user.IamUserRepository;
import com.grab.service.authorizationserver.core.user.SelectedContext;
import com.grab.service.contract.message.account.scim.GrabOidcClaims;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthorizationJwtCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    public static final String VERSION = "authentication_version";

    private final AuthorizationSigningKeys keys;
    private final AuthorizationClientPolicies clients;
    private final IamUserRepository users;

    public AuthorizationJwtCustomizer(AuthorizationSigningKeys keys, AuthorizationClientPolicies clients) {
        this(keys, clients, null);
    }

    public AuthorizationJwtCustomizer(AuthorizationSigningKeys keys, AuthorizationClientPolicies clients, IamUserRepository users) {
        this.keys = keys;
        this.clients = clients;
        this.users = users;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        context.getJwsHeader().keyId(keys.currentId());
        OAuth2Authorization authorization = context.getAuthorization();

        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            context.getJwsHeader().type("at+jwt");
            String clientId = context.getRegisteredClient().getClientId();
            String audience = clients.find(clientId).map(client -> client.audience())
                    .orElseThrow(() -> new IllegalStateException("Token client is not configured"));
            List<String> audiences = new ArrayList<>(1);
            audiences.add(audience);
            context.getClaims().audience(audiences);
            context.getClaims().claims(claims -> {
                if (authorization != null && authorization.getAttribute(VERSION) != null) {
                    claims.put(VERSION, authorization.getAttribute(VERSION));
                }
                enrichWithSelectedContext(claims, authorization);
                if (authorization != null) {
                    enrichWithUserDetails(claims, authorization.getPrincipalName());
                }
            });
        } else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            context.getClaims().claims(claims -> {
                enrichWithSelectedContext(claims, authorization);
                String principalName = context.getPrincipal() != null ? context.getPrincipal().getName()
                        : (authorization != null ? authorization.getPrincipalName() : null);
                if (principalName != null) {
                    enrichWithUserDetails(claims, principalName);
                }
            });

            if (AuthorizationGrantType.REFRESH_TOKEN.equals(context.getAuthorizationGrantType()) && authorization != null) {
                OAuth2Authorization.Token<OidcIdToken> previous = authorization.getToken(OidcIdToken.class);
                if (previous != null) {
                    String sid = previous.getToken().getClaimAsString("sid");
                    if (sid != null) {
                        context.getClaims().claim("sid", sid);
                    }
                }
            }
        }
    }

    private void enrichWithSelectedContext(Map<String, Object> claims, OAuth2Authorization authorization) {
        if (authorization == null) return;
        SelectedContext selected = SelectedContext.from(authorization);
        if (selected == null) return;

        if (selected.onboarding()) {
            claims.put(GrabOidcClaims.ONBOARDING_SCOPE, "true");
            if (selected.platformUserId() != null) {
                claims.put(GrabOidcClaims.PLATFORM_USER_ID, selected.platformUserId());
            }
        } else {
            if (selected.platformUserId() != null) {
                claims.put(GrabOidcClaims.PLATFORM_USER_ID, selected.platformUserId());
            }
            if (selected.assignmentId() != null) {
                claims.put(GrabOidcClaims.ASSIGNMENT_ID, selected.assignmentId());
            }
            if (selected.scopeKey() != null) {
                claims.put(GrabOidcClaims.SCOPE_KEY, selected.scopeKey());
            }
            if (selected.scopeId() != null) {
                claims.put(GrabOidcClaims.SCOPE_ID, selected.scopeId());
            }
            if (selected.role() != null) {
                claims.put(GrabOidcClaims.ROLE, selected.role());
            }
            if (selected.authorities() != null && !selected.authorities().isEmpty()) {
                claims.put(GrabOidcClaims.AUTHORITIES, new ArrayList<>(selected.authorities()));
            }
        }
    }

    private void enrichWithUserDetails(Map<String, Object> claims, String username) {
        if (users == null || username == null) return;
        users.findById(username).or(() -> users.findByUsername(username)).ifPresent(user -> {
            claims.put(GrabOidcClaims.EMAIL, user.email());
            claims.put("email_verified", true);
            if (user.firstName() != null) {
                claims.put("given_name", user.firstName());
            }
            if (user.lastName() != null) {
                claims.put("family_name", user.lastName());
            }
            if (user.firstName() != null || user.lastName() != null) {
                String fullName = ((user.firstName() != null ? user.firstName() : "") + " "
                        + (user.lastName() != null ? user.lastName() : "")).trim();
                claims.put("name", fullName);
            }
        });
    }
}
