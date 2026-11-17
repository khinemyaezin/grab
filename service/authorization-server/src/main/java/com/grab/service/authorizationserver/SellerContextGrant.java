package com.grab.service.authorizationserver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.util.Map;

final class SellerContextGrant extends OAuth2AuthorizationGrantAuthenticationToken {
    static final AuthorizationGrantType TYPE = new AuthorizationGrantType("urn:grab:oauth:grant-type:seller-context");
    private final String subjectToken;
    private final String assignmentId;

    SellerContextGrant(Authentication client, String subjectToken, String assignmentId) {
        super(TYPE, client, Map.of());
        this.subjectToken = subjectToken;
        this.assignmentId = assignmentId;
    }

    String subjectToken() {
        return subjectToken;
    }

    String assignmentId() {
        return assignmentId;
    }

    static final class Converter implements AuthenticationConverter {
        @Override
        public Authentication convert(HttpServletRequest request) {
            String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
            if (!TYPE.getValue().equals(grantType)) {
                return null;
            }
            for (String parameter : request.getParameterMap().keySet()) {
                if (!parameter.equals("grant_type") && !parameter.equals("subject_token")
                        && !parameter.equals("assignment_id") && !parameter.equals("client_id")) {
                    throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
                }
            }
            required(request, OAuth2ParameterNames.GRANT_TYPE, 100);
            String token = required(request, "subject_token", 8192);
            String assignment = required(request, "assignment_id", 255);
            Authentication principal = SecurityContextHolder.getContext().getAuthentication();
            return new SellerContextGrant(principal, token, assignment);
        }

        private String required(HttpServletRequest request, String key, int maxLength) {
            String[] values = request.getParameterValues(key);
            if (values == null || values.length != 1 || values[0].isBlank() || values[0].length() > maxLength) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
            }
            return values[0];
        }
    }
}
