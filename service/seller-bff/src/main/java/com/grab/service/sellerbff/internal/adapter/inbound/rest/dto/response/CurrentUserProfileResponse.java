package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CurrentUserProfileResponse(
        String id,
        String email,
        String status,
        String createdAt,
        List<AccessContextInfo> accessContexts,
        AccessContextInfo currentAccessContext
) {
    @JsonProperty("contextSelectionRequired")
    public boolean contextSelectionRequired() {
        return currentAccessContext == null;
    }

    public record AccessContextInfo(
            String assignmentId,
            String roleCode,
            String scopeKey,
            String scopeId,
            String status
    ) {}
}

