package com.grab.service.authorizationserver.core.user;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public record SelectedContext(
        String assignmentId,
        String platformUserId,
        String scopeKey,
        String scopeId,
        String role,
        String displayName,
        Set<String> authorities,
        boolean onboarding
) implements Serializable {
    public static final String ATTR_ASSIGNMENT_ID = "SELECTED_ASSIGNMENT_ID";
    public static final String ATTR_PLATFORM_USER_ID = "SELECTED_PLATFORM_USER_ID";
    public static final String ATTR_SCOPE_KEY = "SELECTED_SCOPE_KEY";
    public static final String ATTR_SCOPE_ID = "SELECTED_SCOPE_ID";
    public static final String ATTR_ROLE = "SELECTED_ROLE";
    public static final String ATTR_DISPLAY_NAME = "SELECTED_DISPLAY_NAME";
    public static final String ATTR_AUTHORITIES = "SELECTED_AUTHORITIES";
    public static final String ATTR_ONBOARDING = "SELECTED_ONBOARDING";

    public SelectedContext {
        authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
    }

    public static SelectedContext onboarding(String platformUserId) {
        return new SelectedContext(null, platformUserId, null, null, null, "Onboarding", Set.of(), true);
    }

    public OAuth2Authorization storeIn(OAuth2Authorization authorization) {
        OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
        if (assignmentId != null) builder.attribute(ATTR_ASSIGNMENT_ID, assignmentId);
        if (platformUserId != null) builder.attribute(ATTR_PLATFORM_USER_ID, platformUserId);
        if (scopeKey != null) builder.attribute(ATTR_SCOPE_KEY, scopeKey);
        if (scopeId != null) builder.attribute(ATTR_SCOPE_ID, scopeId);
        if (role != null) builder.attribute(ATTR_ROLE, role);
        if (displayName != null) builder.attribute(ATTR_DISPLAY_NAME, displayName);
        if (authorities != null && !authorities.isEmpty()) {
            builder.attribute(ATTR_AUTHORITIES, String.join(",", authorities));
        }
        builder.attribute(ATTR_ONBOARDING, onboarding);
        return builder.build();
    }

    public static SelectedContext from(OAuth2Authorization authorization) {
        if (authorization == null) return null;
        Boolean onboarding = authorization.getAttribute(ATTR_ONBOARDING);
        String assignmentId = authorization.getAttribute(ATTR_ASSIGNMENT_ID);
        String platformUserId = authorization.getAttribute(ATTR_PLATFORM_USER_ID);
        String scopeKey = authorization.getAttribute(ATTR_SCOPE_KEY);
        String scopeId = authorization.getAttribute(ATTR_SCOPE_ID);
        String role = authorization.getAttribute(ATTR_ROLE);
        String displayName = authorization.getAttribute(ATTR_DISPLAY_NAME);
        String authStr = authorization.getAttribute(ATTR_AUTHORITIES);

        if (onboarding == null && assignmentId == null && scopeKey == null && platformUserId == null) {
            return null;
        }

        Set<String> authorities = new HashSet<>();
        if (authStr != null && !authStr.isBlank()) {
            for (String p : authStr.split(",")) {
                if (!p.isBlank()) authorities.add(p.trim());
            }
        }
        return new SelectedContext(assignmentId, platformUserId, scopeKey, scopeId, role, displayName,
                authorities, Boolean.TRUE.equals(onboarding));
    }
}
