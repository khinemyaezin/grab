package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record RoleDetailsUpdatedEvent(
        Id roleId,
        String name,
        String description,
        LocalDateTime occurredAt
) implements Event {
}
