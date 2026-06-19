package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record RoleStatusChangedEvent(
        Id roleId,
        boolean active,
        LocalDateTime occurredAt
) implements Event {
}
