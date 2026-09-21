package com.grab.store.identity.internal.event;

import com.grab.store.identity.events.UserRegisteredIntegrationEvent;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
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
    private final AccessAssignmentQueryPort accessAssignmentQueryPort;

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        var userId = event.userId().getValue();
        var assignments = accessAssignmentQueryPort.findByUser(userId);
        var platformCode = assignments.isEmpty() ? null : assignments.getFirst().platformCode();
        var integrationEvent = new UserRegisteredIntegrationEvent(
                userId,
                event.email(),
                platformCode,
                Instant.now(),
                EVENT_VERSION
        );
        events.publishEvent(integrationEvent);
    }
}
