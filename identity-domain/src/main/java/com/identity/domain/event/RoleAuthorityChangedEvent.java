package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record RoleAuthorityChangedEvent(
        Id roleId,
        String authorityCode,
        boolean assigned,
        LocalDateTime occurredAt
) implements Event {
}
