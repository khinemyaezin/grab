package com.grab.store.customer.internal.event;

import com.customer.domain.event.CustomerRegisteredEvent;
import com.customer.domain.event.GuestCustomerCreatedEvent;
import com.grab.store.customer.events.CustomerRegisteredIntegrationEvent;
import com.grab.store.customer.events.GuestCustomerCreatedIntegrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    @EventListener
    public void handleCustomerRegistered(CustomerRegisteredEvent event) {
        events.publishEvent(new CustomerRegisteredIntegrationEvent(
                event.customerId(),
                event.userId(),
                event.email(),
                event.displayName(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleGuestCustomerCreated(GuestCustomerCreatedEvent event) {
        events.publishEvent(new GuestCustomerCreatedIntegrationEvent(
                event.customerId(),
                event.email(),
                event.displayName(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }
}
