package com.grab.framework.outbox;

import com.grab.framework.domain.Event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.Base64;

public class JavaSerializationOutboxEventSerializer implements OutboxEventSerializer {

    @Override
    public SerializedEvent serialize(Event event) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(output)) {
            objectStream.writeObject(event);
            objectStream.flush();
            return new SerializedEvent(
                    event.getClass().getName(),
                    Base64.getEncoder().encodeToString(output.toByteArray()),
                    1,
                    "{}"
            );
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to serialize outbox event " + event.getClass().getName(), exception);
        }
    }

    @Override
    public Event deserialize(SerializedEvent serializedEvent) {
        String eventType = serializedEvent.eventType();
        byte[] bytes = Base64.getDecoder().decode(serializedEvent.payload());

        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes);
             ObjectInputStream objectStream = new ObjectInputStream(input)) {
            Object value = objectStream.readObject();
            if (!(value instanceof Event event)) {
                throw new IllegalStateException("Outbox payload is not an Event: " + eventType);
            }

            if (!event.getClass().getName().equals(eventType)) {
                throw new IllegalStateException("Outbox payload type mismatch. Expected " + eventType
                        + " but found " + event.getClass().getName());
            }

            return event;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to deserialize outbox event " + eventType, exception);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Outbox event class not found " + eventType, exception);
        }
    }
}
