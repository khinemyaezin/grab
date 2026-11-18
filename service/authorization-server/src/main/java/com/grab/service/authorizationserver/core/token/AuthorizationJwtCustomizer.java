package com.grab.service.authorizationserver.core.token;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import com.grab.service.authorizationserver.config.AuthorizationClientPolicies;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationJwtCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    public static final String VERSION = "authentication_version";

    private final AuthorizationSigningKeys keys;
    private final AuthorizationClientPolicies clients;

    public AuthorizationJwtCustomizer(AuthorizationSigningKeys keys, AuthorizationClientPolicies clients) {
        this.keys = keys;
        this.clients = clients;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        context.getJwsHeader().keyId(keys.currentId());
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            OAuth2Authorization authorization = context.getAuthorization();
            context.getJwsHeader().type("at+jwt");
            String clientId = context.getRegisteredClient().getClientId();
            String audience = clients.find(clientId).map(client -> client.audience())
                    .orElseThrow(() -> new IllegalStateException("Token client is not configured"));
            List<String> audiences = new ArrayList<>(1);
            audiences.add(audience);
            context.getClaims().audience(audiences);
            context.getClaims().claims(claims -> {
                claims.put(VERSION, authorization.getAttribute(VERSION));
            });
        } else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())
                && AuthorizationGrantType.REFRESH_TOKEN.equals(context.getAuthorizationGrantType())) {
            OAuth2Authorization.Token<OidcIdToken> previous = context.getAuthorization().getToken(OidcIdToken.class);
            if (previous != null) {
                String sid = previous.getToken().getClaimAsString("sid");
                if (sid != null) {
                    context.getClaims().claim("sid", sid);
                }
            }
        }
    }
}
