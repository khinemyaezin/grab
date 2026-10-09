package com.identity.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.Objects;

public record FulfillAdminAccessAssignmentCommand(
        String requestId,
        Id merchantId,
        Id applicantUserId,
        String roleCode,
        String scopeKey,
        long memberVersion,
        Instant requestedAt
) implements Command<AccessAssignmentResult> {
    public FulfillAdminAccessAssignmentCommand {
        Objects.requireNonNull(requestId, "requestId is required");
        Objects.requireNonNull(merchantId, "merchantId is required");
        Objects.requireNonNull(applicantUserId, "applicantUserId is required");
        Objects.requireNonNull(roleCode, "roleCode is required");
        Objects.requireNonNull(scopeKey, "scopeKey is required");
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }
}
