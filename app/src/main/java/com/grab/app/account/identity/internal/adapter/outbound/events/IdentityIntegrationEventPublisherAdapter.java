package com.grab.app.account.identity.internal.adapter.outbound.events;

import com.grab.app.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent;
import com.grab.app.shared.events.identity.UserRegisteredIntegrationEvent;
import com.identity.application.port.outbound.IdentityIntegrationEventPublisher;
import com.identity.domain.event.SecurityCatalogActivatedEvent;
import com.identity.domain.event.UserRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class IdentityIntegrationEventPublisherAdapter implements IdentityIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    public IdentityIntegrationEventPublisherAdapter(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    @EventListener
    public void publishUserRegistered(UserRegisteredEvent event) {
        String userId = event.userId() != null ? event.userId().getValue() : null;
        var integrationEvent = new UserRegisteredIntegrationEvent(
                userId,
                event.email(),
                Instant.now(),
                EVENT_VERSION
        );
        events.publishEvent(integrationEvent);
    }

    @Override
    @EventListener
    public void publishSecurityCatalogActivated(SecurityCatalogActivatedEvent event) {
        events.publishEvent(new IdentitySecurityCatalogActivatedIntegrationEvent(
                event.catalogRevision(),
                event.moduleKey(),
                event.securityRevision(),
                event.contentDigest()
        ));
    }
}
