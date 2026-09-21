package com.grab.store.identity.internal.event;

import com.grab.store.identity.events.UserRegisteredIntegrationEvent;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserRegistrationIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    public UserProfileResult afterRegistration(RegisterCommand command, UserProfileResult result) {
        events.publishEvent(new UserRegisteredIntegrationEvent(
                result.id(),
                result.email(),
                command.platformCode(),
                Instant.now(),
                EVENT_VERSION
        ));
        return result;
    }
}
