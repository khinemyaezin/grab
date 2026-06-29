package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.identity.domain.enums.UserStatus;

import java.time.LocalDateTime;
public record UserRegisteredEvent(
        Id userId,
        String email,
        UserStatus status,
        LocalDateTime occurredAt
) implements Event {
}
