package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

import java.util.List;

public record UserProfileDetailResponse(
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
    ) {}
}

