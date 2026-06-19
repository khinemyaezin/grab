package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.identity.domain.enums.UserStatus;

import java.time.LocalDateTime;

public record UserStatusChangedEvent(
        Id userId,
        UserStatus previousStatus,
        UserStatus currentStatus,
        LocalDateTime occurredAt
) implements Event {
}
