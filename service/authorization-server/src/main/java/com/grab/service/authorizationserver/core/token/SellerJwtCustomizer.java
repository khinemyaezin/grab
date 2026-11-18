package com.grab.service.authorizationserver.core.token;

import com.grab.service.authorizationserver.config.SellerIssuerProperties;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Collections;

public class SellerJwtCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    public static final String VERSION = "authentication_version";
    public static final String ASSIGNMENT = "assignment_id";
    public static final String SCOPE_KEY = "scope_key";
    public static final String SCOPE_ID = "scope_id";

    private final SellerSigningKeys keys;
    private final SellerIssuerProperties properties;

    public SellerJwtCustomizer(SellerSigningKeys keys, SellerIssuerProperties properties) {
        this.keys = keys;
        this.properties = properties;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        context.getJwsHeader().keyId(keys.currentId());
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            OAuth2Authorization authorization = context.getAuthorization();
            context.getJwsHeader().type("at+jwt");
            context.getClaims().audience(Collections.singletonList(properties.audience()));
            context.getClaims().claims(claims -> {
                claims.put(VERSION, authorization.getAttribute(VERSION));
                String assignment = authorization.getAttribute(ASSIGNMENT);
                if (assignment != null) {
                    claims.put(ASSIGNMENT, assignment);
                    claims.put(SCOPE_KEY, authorization.getAttribute(SCOPE_KEY));
                    claims.put(SCOPE_ID, authorization.getAttribute(SCOPE_ID));
                }
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
