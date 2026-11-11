package com.grab.app.account.customer.internal.adapter.outbound.events;

import com.customer.application.port.outbound.CustomerIntegrationEventPublisher;
import com.customer.domain.event.CustomerRegisteredEvent;
import com.customer.domain.event.GuestCustomerCreatedEvent;
import com.grab.app.shared.events.customer.CustomerRegisteredIntegrationEvent;
import com.grab.app.shared.events.customer.GuestCustomerCreatedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerIntegrationEventPublisherAdapter implements CustomerIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    public CustomerIntegrationEventPublisherAdapter(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    @EventListener
    public void publishCustomerRegistered(CustomerRegisteredEvent event) {
        events.publishEvent(new CustomerRegisteredIntegrationEvent(
                event.customerId(),
                event.userId(),
                event.email(),
                event.displayName(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }

    @Override
    @EventListener
    public void publishGuestCustomerCreated(GuestCustomerCreatedEvent event) {
        events.publishEvent(new GuestCustomerCreatedIntegrationEvent(
                event.customerId(),
                event.email(),
                event.displayName(),
                event.occurredAt(),
                EVENT_VERSION
        ));
    }
}
