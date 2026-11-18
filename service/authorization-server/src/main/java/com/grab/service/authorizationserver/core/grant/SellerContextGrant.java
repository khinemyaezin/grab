package com.grab.service.authorizationserver.core.grant;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;

public final class SellerContextGrant extends OAuth2AuthorizationGrantAuthenticationToken {
    public static final AuthorizationGrantType TYPE = new AuthorizationGrantType("urn:grab:oauth:grant-type:seller-context");
    private final String subjectToken;
    private final String assignmentId;

    public SellerContextGrant(Authentication client, String subjectToken, String assignmentId) {
        super(TYPE, client, Map.of());
        this.subjectToken = subjectToken;
        this.assignmentId = assignmentId;
    }

    public String subjectToken() {
        return subjectToken;
    }

    public String assignmentId() {
        return assignmentId;
    }
}
