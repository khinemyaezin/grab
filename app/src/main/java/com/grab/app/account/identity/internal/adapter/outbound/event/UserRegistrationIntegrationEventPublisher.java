package com.grab.app.account.identity.internal.adapter.outbound.event;

import com.grab.app.shared.events.identity.UserRegisteredIntegrationEvent;
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
