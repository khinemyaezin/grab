package com.grab.app.identityaccount.identity.internal.api.rest.dto.response;

import java.util.List;

public record UserProfileResponse(
        String id,
        String email,
        String status,
        String createdAt,
        List<AccessContextInfo> accessContexts
) {
    public record AccessContextInfo(
            String assignmentId,
            String roleCode,
            String scopeKey,
            String scopeId,
            String status
    ){}
}
