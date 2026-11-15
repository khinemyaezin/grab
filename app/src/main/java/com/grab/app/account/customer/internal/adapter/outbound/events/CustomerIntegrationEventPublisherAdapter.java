package com.grab.app.account.customer.internal.adapter.outbound.events;

import com.customer.application.port.outbound.CustomerIntegrationEventPublisher;
import com.customer.domain.event.CustomerRegisteredEvent;
import com.customer.domain.event.GuestCustomerCreatedEvent;
import com.grab.app.shared.events.customer.CustomerRegisteredIntegrationEvent;
import com.grab.app.shared.events.customer.GuestCustomerCreatedIntegrationEvent;
import com.grab.framework.event.DomainEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerIntegrationEventPublisherAdapter implements CustomerIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final DomainEventProducer outbox;

    public CustomerIntegrationEventPublisherAdapter(
            @Qualifier("customerDomainEventProducer") DomainEventProducer outbox
    ) {
        this.outbox = outbox;
    }

    @Override
    @EventListener
    public void publishCustomerRegistered(CustomerRegisteredEvent event) {
        var integrationEvent = new CustomerRegisteredIntegrationEvent(
                event.customerId(),
                event.userId(),
                event.email(),
                event.displayName(),
                event.occurredAt(),
                EVENT_VERSION
        );
        outbox.produce("Customer", event.customerId(), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishGuestCustomerCreated(GuestCustomerCreatedEvent event) {
        var integrationEvent = new GuestCustomerCreatedIntegrationEvent(
                event.customerId(),
                event.email(),
                event.displayName(),
                event.occurredAt(),
                EVENT_VERSION
        );
        outbox.produce("Customer", event.customerId(), List.of(integrationEvent));
    }
}
