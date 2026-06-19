package com.identity.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.identity.domain.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record UserRegisteredEvent(
        Id userId,
        String email,
        Set<String> roleCodes,
        UserStatus status,
        LocalDateTime occurredAt
) implements Event {
    public UserRegisteredEvent {
        roleCodes = Set.copyOf(roleCodes);
    }
}
