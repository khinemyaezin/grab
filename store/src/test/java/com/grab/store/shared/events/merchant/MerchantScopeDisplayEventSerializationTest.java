package com.grab.store.shared.events.merchant;

import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.store.shared.events.identity.ScopeDisplayChangedIntegrationEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantScopeDisplayEventSerializationTest {
    private final JsonOutboxEventSerializer serializer = new JsonOutboxEventSerializer();

    @Test
    void merchantDisplaySnapshot_roundTripsThroughOutboxSerializer() {
        ScopeDisplayChangedIntegrationEvent event = new ScopeDisplayChangedIntegrationEvent(
                "merchant-1", "Shop");

        SerializedEvent serialized = serializer.serialize(event);
        Object deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void merchantProfileUpdatedEvent_roundTripsThroughOutboxSerializer() {
        MerchantProfileUpdatedEvent event = new MerchantProfileUpdatedEvent(
                "merchant-1", "Shop", 8, Instant.parse("2026-10-11T10:00:00Z"));

        SerializedEvent serialized = serializer.serialize(event);
        Object deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(event);
    }
}
