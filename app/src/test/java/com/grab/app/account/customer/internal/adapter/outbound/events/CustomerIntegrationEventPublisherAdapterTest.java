package com.grab.app.account.customer.internal.adapter.outbound.events;

import com.customer.domain.event.CustomerRegisteredEvent;
import com.customer.domain.event.GuestCustomerCreatedEvent;
import com.grab.app.shared.events.customer.CustomerRegisteredIntegrationEvent;
import com.grab.app.shared.events.customer.GuestCustomerCreatedIntegrationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerIntegrationEventPublisherAdapterTest {

    private static final Instant OCCURRED_AT = Instant.parse("2026-10-01T10:00:00Z");

    @Mock
    private ApplicationEventPublisher events;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    private CustomerIntegrationEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerIntegrationEventPublisherAdapter(events);
    }

    @Test
    void publishCustomerRegistered_publishesIntegrationEvent() {
        CustomerRegisteredEvent domainEvent = new CustomerRegisteredEvent(
                "cust-1", "user-1", "alice@example.com", "Alice", OCCURRED_AT
        );

        adapter.publishCustomerRegistered(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(CustomerRegisteredIntegrationEvent.class, event -> {
            assertThat(event.customerId()).isEqualTo("cust-1");
            assertThat(event.userId()).isEqualTo("user-1");
            assertThat(event.email()).isEqualTo("alice@example.com");
            assertThat(event.displayName()).isEqualTo("Alice");
            assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
            assertThat(event.version()).isEqualTo(1);
        });
    }

    @Test
    void publishGuestCustomerCreated_publishesIntegrationEvent() {
        GuestCustomerCreatedEvent domainEvent = new GuestCustomerCreatedEvent(
                "guest-1", "guest@example.com", "Guest User", OCCURRED_AT
        );

        adapter.publishGuestCustomerCreated(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(GuestCustomerCreatedIntegrationEvent.class, event -> {
            assertThat(event.customerId()).isEqualTo("guest-1");
            assertThat(event.email()).isEqualTo("guest@example.com");
            assertThat(event.displayName()).isEqualTo("Guest User");
            assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
            assertThat(event.version()).isEqualTo(1);
        });
    }
}
