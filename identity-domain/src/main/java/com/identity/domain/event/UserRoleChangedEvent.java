package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record UserRoleChangedEvent(
        Id userId,
        String roleCode,
        boolean assigned,
        LocalDateTime occurredAt
) implements Event {
}
