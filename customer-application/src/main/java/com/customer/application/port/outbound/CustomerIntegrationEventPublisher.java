package com.customer.application.port.outbound;

import com.customer.domain.event.CustomerRegisteredEvent;
import com.customer.domain.event.GuestCustomerCreatedEvent;

public interface CustomerIntegrationEventPublisher {

    void publishCustomerRegistered(CustomerRegisteredEvent event);

    void publishGuestCustomerCreated(GuestCustomerCreatedEvent event);
}

