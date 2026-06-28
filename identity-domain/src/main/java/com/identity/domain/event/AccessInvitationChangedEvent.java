package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.identity.domain.enums.InvitationStatus;

import java.time.Instant;

public record AccessInvitationChangedEvent(
        Id invitationId,
        String inviteeEmail,
        String platformCode,
        String roleCode,
        String scopeKey,
        String scopeId,
        InvitationStatus previousStatus,
        InvitationStatus currentStatus,
        Instant occurredAt
) implements Event {
}
