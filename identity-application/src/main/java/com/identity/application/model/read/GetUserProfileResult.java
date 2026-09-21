package com.identity.application.model.read;

import java.util.List;

public record GetUserProfileResult(
        String id,
        String email,
        String status,
        String createdAt,
        List<AccessContextInfo> accessContexts

) {
    public record AccessContextInfo(
            String assignmentId,
            String platformCode,
            String roleCode,
            String scopeKey,
            String scopeId,
            String status
    ){}
}
