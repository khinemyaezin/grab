package com.grab.app.account.identity.internal.adapter.outbound.events;

import com.grab.app.shared.events.identity.IdentitySecurityCatalogActivatedIntegrationEvent;
import com.grab.app.shared.events.identity.UserRegisteredIntegrationEvent;
import com.grab.framework.id.impl.CommonId;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.event.SecurityCatalogActivatedEvent;
import com.identity.domain.event.UserRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IdentityIntegrationEventPublisherAdapterTest {

    @Mock
    private ApplicationEventPublisher events;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    private IdentityIntegrationEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new IdentityIntegrationEventPublisherAdapter(events);
    }

    @Test
    void publishUserRegistered_publishesIntegrationEvent() {
        UserRegisteredEvent domainEvent = new UserRegisteredEvent(
                new CommonId("user-1"), "user@example.com", UserStatus.ACTIVE, LocalDateTime.now()
        );

        adapter.publishUserRegistered(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(UserRegisteredIntegrationEvent.class, event -> {
            assertThat(event.userId()).isEqualTo("user-1");
            assertThat(event.email()).isEqualTo("user@example.com");
            assertThat(event.version()).isEqualTo(1);
        });
    }

    @Test
    void publishSecurityCatalogActivated_publishesIntegrationEvent() {
        SecurityCatalogActivatedEvent domainEvent = new SecurityCatalogActivatedEvent(
                1L, "catalog-module", 2, "digest-abc"
        );

        adapter.publishSecurityCatalogActivated(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(IdentitySecurityCatalogActivatedIntegrationEvent.class, event -> {
            assertThat(event.catalogRevision()).isEqualTo(1L);
            assertThat(event.moduleKey()).isEqualTo("catalog-module");
            assertThat(event.securityRevision()).isEqualTo(2);
            assertThat(event.contentDigest()).isEqualTo("digest-abc");
        });
    }
}
