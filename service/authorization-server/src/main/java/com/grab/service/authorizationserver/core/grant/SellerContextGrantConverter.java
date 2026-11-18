package com.grab.service.authorizationserver.core.grant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;

public final class SellerContextGrantConverter implements AuthenticationConverter {
    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!SellerContextGrant.TYPE.getValue().equals(grantType)) {
            return null;
        }
        for (String parameter : request.getParameterMap().keySet()) {
            if (!parameter.equals("grant_type") && !parameter.equals("subject_token")
                    && !parameter.equals("assignment_id") && !parameter.equals("client_id")
                    && !parameter.equals("client_secret")) {
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
