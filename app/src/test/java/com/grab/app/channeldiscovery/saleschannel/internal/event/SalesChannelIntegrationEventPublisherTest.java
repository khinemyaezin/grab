package com.grab.app.channeldiscovery.saleschannel.internal.event;

import com.grab.app.shared.events.saleschannel.SalesChannelDisabledIntegrationEvent;
import com.grab.app.shared.events.saleschannel.SalesChannelEnabledIntegrationEvent;
import com.saleschannel.domain.event.SalesChannelDisabledEvent;
import com.saleschannel.domain.event.SalesChannelEnabledEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SalesChannelIntegrationEventPublisherTest {

    @Mock
    private ApplicationEventPublisher events;
    @Captor
    private ArgumentCaptor<Object> published;
    @InjectMocks
    private SalesChannelIntegrationEventPublisher publisher;

    @Test
    void handleEnabled_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleEnabled(new SalesChannelEnabledEvent(
                "channel-1", "WEBSITE", "merchant-1", Instant.now()));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(SalesChannelEnabledIntegrationEvent.class, event ->
                assertThat(event.salesChannelId()).isEqualTo("channel-1"));
    }

    @Test
    void handleDisabled_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleDisabled(new SalesChannelDisabledEvent(
                "channel-1", "WEBSITE", "merchant-1", Instant.now()));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(SalesChannelDisabledIntegrationEvent.class, event ->
                assertThat(event.salesChannelId()).isEqualTo("channel-1"));
    }
}
