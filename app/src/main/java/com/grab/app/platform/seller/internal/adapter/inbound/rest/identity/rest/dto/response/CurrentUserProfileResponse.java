package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response;

import java.util.List;

public record CurrentUserProfileResponse(
        String id,
        String email,
        String status,
        String createdAt,
        List<AccessContextInfo> accessContexts,
        AccessContextInfo currentAccessContext
) {
    public record AccessContextInfo(
            String assignmentId,
            String roleCode,
            String scopeKey,
            String scopeId,
            String status
    ){}
}
