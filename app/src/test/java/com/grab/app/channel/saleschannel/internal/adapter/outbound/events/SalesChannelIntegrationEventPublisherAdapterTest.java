package com.grab.app.channel.saleschannel.internal.adapter.outbound.events;

import com.grab.app.shared.events.saleschannel.SalesChannelDisabledIntegrationEvent;
import com.grab.app.shared.events.saleschannel.SalesChannelEnabledIntegrationEvent;
import com.saleschannel.domain.event.SalesChannelDisabledEvent;
import com.saleschannel.domain.event.SalesChannelEnabledEvent;
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
class SalesChannelIntegrationEventPublisherAdapterTest {

    private static final Instant OCCURRED_AT = Instant.parse("2026-10-01T12:00:00Z");

    @Mock
    private ApplicationEventPublisher events;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    private SalesChannelIntegrationEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SalesChannelIntegrationEventPublisherAdapter(events);
    }

    @Test
    void publishSalesChannelEnabled_publishesIntegrationEvent() {
        SalesChannelEnabledEvent domainEvent = new SalesChannelEnabledEvent(
                "channel-1", "WEBSITE", "merchant-1", OCCURRED_AT
        );

        adapter.publishSalesChannelEnabled(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(SalesChannelEnabledIntegrationEvent.class, event -> {
            assertThat(event.salesChannelId()).isEqualTo("channel-1");
            assertThat(event.type()).isEqualTo("WEBSITE");
            assertThat(event.merchantId()).isEqualTo("merchant-1");
            assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
            assertThat(event.version()).isEqualTo(1);
        });
    }

    @Test
    void publishSalesChannelDisabled_publishesIntegrationEvent() {
        SalesChannelDisabledEvent domainEvent = new SalesChannelDisabledEvent(
                "channel-1", "WEBSITE", "merchant-1", OCCURRED_AT
        );

        adapter.publishSalesChannelDisabled(domainEvent);

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(SalesChannelDisabledIntegrationEvent.class, event -> {
            assertThat(event.salesChannelId()).isEqualTo("channel-1");
            assertThat(event.type()).isEqualTo("WEBSITE");
            assertThat(event.merchantId()).isEqualTo("merchant-1");
            assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
            assertThat(event.version()).isEqualTo(1);
        });
    }
}
