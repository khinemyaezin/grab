package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.identity.domain.enums.AccessAssignmentStatus;

import java.time.Instant;

public record AccessAssignmentChangedEvent(
        Id assignmentId,
        Id userId,
        String platformCode,
        String roleCode,
        String scopeKey,
        String scopeId,
        AccessAssignmentStatus previousStatus,
        AccessAssignmentStatus currentStatus,
        Instant occurredAt
) implements Event {
}
