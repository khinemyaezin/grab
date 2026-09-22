package com.grab.store.identity.internal.event;

import com.grab.store.shared.events.UserRegisteredIntegrationEvent;
import com.identity.domain.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserRegistrationIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        var userId = event.userId().getValue();
        var integrationEvent = new UserRegisteredIntegrationEvent(
                userId,
                event.email(),
                Instant.now(),
                EVENT_VERSION
        );
        events.publishEvent(integrationEvent);
    }
}
