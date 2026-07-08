package com.identity.infrastructure.view;

import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.enums.UserStatus;

import java.time.LocalDateTime;

public record UserAssignmentView(
        String userId,
        String email,
        String userStatus,
        String createdAt,
        String assignmentId,
        String platformCode,
        String roleCode,
        String scopeKey,
        String scopeId,
        String assignmentStatus
) {
    public UserAssignmentView(
            String userId,
            String email,
            UserStatus userStatus,
            LocalDateTime createdAt,
            String assignmentId,
            String platformCode,
            String roleCode,
            String scopeKey,
            String scopeId,
            AccessAssignmentStatus assignmentStatus
    ) {
        this(
                userId,
                email,
                userStatus != null ? userStatus.name() : null,
                createdAt != null ? createdAt.toString() : null,
                assignmentId,
                platformCode,
                roleCode,
                scopeKey,
                scopeId,
                assignmentStatus != null ? assignmentStatus.name() : null
        );
    }
}
