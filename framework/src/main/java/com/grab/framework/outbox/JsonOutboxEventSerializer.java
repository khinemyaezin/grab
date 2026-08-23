package com.grab.framework.outbox;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonOutboxEventSerializer implements OutboxEventSerializer {

    private static final int EVENT_VERSION = 1;
    private static final String CONTENT_TYPE_KEY = "contentType";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String LEGACY_HEADERS = "{}";

    private final ObjectMapper objectMapper;
    private final OutboxEventSerializer legacySerializer;

    public JsonOutboxEventSerializer() {
        this(defaultObjectMapper(), new JavaSerializationOutboxEventSerializer());
    }

    public JsonOutboxEventSerializer(ObjectMapper objectMapper, OutboxEventSerializer legacySerializer) {
        this.objectMapper = objectMapper;
        this.legacySerializer = legacySerializer;
    }

    @Override
    public SerializedEvent serialize(Event event) {
        try {
            return new SerializedEvent(
                    event.getClass().getName(),
                    objectMapper.writeValueAsString(event),
                    EVENT_VERSION,
                    objectMapper.writeValueAsString(jsonHeaders())
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize outbox event " + event.getClass().getName(), exception);
        }
    }

    @Override
    public Event deserialize(SerializedEvent serializedEvent) {
        if (!isJsonPayload(serializedEvent.headers())) {
            return legacySerializer.deserialize(serializedEvent);
        }

        try {
            Class<?> eventClass = Class.forName(serializedEvent.eventType());
            if (!Event.class.isAssignableFrom(eventClass)) {
                throw new IllegalStateException("Outbox payload is not an Event: " + serializedEvent.eventType());
            }

            return objectMapper.readValue(serializedEvent.payload(), eventClass.asSubclass(Event.class));
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Outbox event class not found " + serializedEvent.eventType(), exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to deserialize outbox event " + serializedEvent.eventType(), exception);
        }
    }

    private static Map<String, String> jsonHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(CONTENT_TYPE_KEY, JSON_CONTENT_TYPE);
        return headers;
    }

    private boolean isJsonPayload(String headers) {
        if (headers == null || headers.isBlank() || LEGACY_HEADERS.equals(headers.trim())) {
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(headers);
            JsonNode contentType = root.get(CONTENT_TYPE_KEY);
            return contentType != null && JSON_CONTENT_TYPE.equalsIgnoreCase(contentType.asText());
        } catch (IOException exception) {
            return false;
        }
    }

    private static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(idModule());
        return mapper;
    }

    private static SimpleModule idModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Id.class, new JsonSerializer<>() {
            @Override
            public void serialize(Id value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                    return;
                }
                gen.writeString(value.getValue());
            }
        });
        module.addDeserializer(Id.class, new JsonDeserializer<>() {
            @Override
            public Id deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
                JsonNode node = parser.getCodec().readTree(parser);
                if (node == null || node.isNull()) {
                    return null;
                }
                if (node.isTextual()) {
                    return new CommonId(node.asText());
                }
                if (node.isObject()) {
                    JsonNode valueNode = node.get("value");
                    if (valueNode != null && valueNode.isTextual()) {
                        return new CommonId(valueNode.asText());
                    }
                    JsonNode idNode = node.get("id");
                    if (idNode != null && idNode.isTextual()) {
                        return new CommonId(idNode.asText());
                    }
                }
                throw new IOException("Unable to deserialize Id from payload: " + node);
            }
        });
        return module;
    }
}
