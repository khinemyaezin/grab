package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.enums.AccessScopeType;

import java.time.Instant;

public record AccessAssignmentChangedEvent(
        Id assignmentId,
        Id userId,
        String platformCode,
        String roleCode,
        AccessScopeType scopeType,
        String scopeId,
        AccessAssignmentStatus previousStatus,
        AccessAssignmentStatus currentStatus,
        Instant occurredAt
) implements Event {
}
