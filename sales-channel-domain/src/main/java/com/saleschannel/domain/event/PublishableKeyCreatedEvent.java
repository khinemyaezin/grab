package com.saleschannel.domain.event;

import com.grab.framework.domain.Event;

import java.time.Instant;

public record PublishableKeyCreatedEvent(String keyId, String salesChannelId, Instant occurredAt) implements Event {
}
