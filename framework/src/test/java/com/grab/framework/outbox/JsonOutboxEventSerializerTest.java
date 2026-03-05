package com.grab.framework.outbox;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonOutboxEventSerializerTest {

    private final JsonOutboxEventSerializer serializer = new JsonOutboxEventSerializer();

    @Test
    void serialize_writesJsonPayloadAndJsonHeader() {
        TestEvent event = new TestEvent(new CommonId("event-1"), "created", LocalDateTime.of(2026, 3, 5, 18, 0));

        SerializedEvent serialized = serializer.serialize(event);

        assertEquals(TestEvent.class.getName(), serialized.eventType());
        assertEquals(1, serialized.eventVersion());
        assertTrue(serialized.payload().contains("\"name\":\"created\""));
        assertTrue(serialized.headers().contains("\"contentType\":\"application/json\""));
    }

    @Test
    void deserialize_readsJsonPayload() {
        TestEvent event = new TestEvent(new CommonId("event-1"), "updated", LocalDateTime.of(2026, 3, 5, 18, 5));
        SerializedEvent serialized = serializer.serialize(event);

        Event deserialized = serializer.deserialize(serialized);

        assertEquals(event, deserialized);
    }

    @Test
    void deserialize_readsLegacyJavaPayloadWhenHeadersAreLegacy() {
        TestEvent event = new TestEvent(new CommonId("event-2"), "legacy", LocalDateTime.of(2026, 3, 5, 18, 10));
        SerializedEvent legacySerialized = new JavaSerializationOutboxEventSerializer().serialize(event);

        Event deserialized = serializer.deserialize(legacySerialized);

        assertEquals(event, deserialized);
    }

    @Test
    void deserialize_withInvalidJsonPayload_throwsIllegalState() {
        SerializedEvent invalid = new SerializedEvent(
                TestEvent.class.getName(),
                "{\"invalid_json\":",
                1,
                "{\"contentType\":\"application/json\"}"
        );

        assertThrows(IllegalStateException.class, () -> serializer.deserialize(invalid));
    }

    private record TestEvent(
            Id eventId,
            String name,
            LocalDateTime occurredAt
    ) implements Event {
    }
}
