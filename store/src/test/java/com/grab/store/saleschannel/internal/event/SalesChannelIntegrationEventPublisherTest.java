package com.grab.store.saleschannel.internal.event;

import com.grab.store.saleschannel.events.SalesChannelDisabledIntegrationEvent;
import com.grab.store.saleschannel.events.SalesChannelEnabledIntegrationEvent;
import com.saleschannel.domain.event.SalesChannelDisabledEvent;
import com.saleschannel.domain.event.SalesChannelEnabledEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class SalesChannelIntegrationEventPublisherTest {

    private final AtomicReference<Object> published = new AtomicReference<>();
    private final ApplicationEventPublisher events = published::set;
    private final SalesChannelIntegrationEventPublisher publisher = new SalesChannelIntegrationEventPublisher(events);

    @Test
    void handleEnabled_shouldPublishIntegrationEvent() {
        publisher.handleEnabled(new SalesChannelEnabledEvent(
                "channel-1", "WEBSITE", "merchant-1", Instant.now()));

        assertThat(published.get()).isInstanceOfSatisfying(SalesChannelEnabledIntegrationEvent.class, event ->
                assertThat(event.salesChannelId()).isEqualTo("channel-1"));
    }

    @Test
    void handleDisabled_shouldPublishIntegrationEvent() {
        publisher.handleDisabled(new SalesChannelDisabledEvent(
                "channel-1", "WEBSITE", "merchant-1", Instant.now()));

        assertThat(published.get()).isInstanceOfSatisfying(SalesChannelDisabledIntegrationEvent.class, event ->
                assertThat(event.salesChannelId()).isEqualTo("channel-1"));
    }
}
