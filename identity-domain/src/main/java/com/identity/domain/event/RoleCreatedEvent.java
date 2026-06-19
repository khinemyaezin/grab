package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;
import java.util.Set;

public record RoleCreatedEvent(
        Id roleId,
        String code,
        String name,
        String description,
        boolean active,
        Set<String> authorityCodes,
        LocalDateTime occurredAt
) implements Event {
    public RoleCreatedEvent {
        authorityCodes = Set.copyOf(authorityCodes);
    }
}
