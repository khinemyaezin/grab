package com.grab.app.shared.events.merchant;

import com.grab.framework.domain.Event;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.SerializedEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantProfileIntegrationEventSerializationTest {
    private static final Instant OCCURRED_AT = Instant.parse("2026-10-11T10:00:00Z");

    private final JsonOutboxEventSerializer serializer = new JsonOutboxEventSerializer();

    @ParameterizedTest
    @MethodSource("integrationEvents")
    void serialize_businessIntegrationEvent_roundTripsThroughOutboxSerializer(Event event) {
        SerializedEvent serialized = serializer.serialize(event);
        Object deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void serialize_pendingDomainEvent_remainsReplayable() {
        MerchantProfileUpdatedEvent event = new MerchantProfileUpdatedEvent(
                "merchant-1", "Shop", 8, OCCURRED_AT);

        SerializedEvent serialized = serializer.serialize(event);
        Object deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(event);
    }

    private static Stream<Event> integrationEvents() {
        MerchantApplicationStartedIntegrationEvent started = new MerchantApplicationStartedIntegrationEvent(
                "merchant-1", "Shop", 1, OCCURRED_AT);
        MerchantProfileUpdatedIntegrationEvent updated = new MerchantProfileUpdatedIntegrationEvent(
                "merchant-1", "New Shop", 8, OCCURRED_AT);
        StorefrontCreatedIntegrationEvent created = new StorefrontCreatedIntegrationEvent(
                "storefront-1", "merchant-1", "Store", "store-slug", "DRAFT", OCCURRED_AT, 1);
        StorefrontRenamedIntegrationEvent renamed = new StorefrontRenamedIntegrationEvent(
                "storefront-1", "merchant-1", "New Store", "new-slug", 9, OCCURRED_AT);
        return Stream.of(started, updated, created, renamed);
    }
}
