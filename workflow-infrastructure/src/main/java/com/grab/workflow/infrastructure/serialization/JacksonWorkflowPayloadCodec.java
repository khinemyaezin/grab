package com.grab.workflow.infrastructure.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grab.framework.workflow.WorkflowPayloadCodec;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowCodecUnavailableException;

import java.util.Objects;

public final class JacksonWorkflowPayloadCodec implements WorkflowPayloadCodec {

    private final ObjectMapper objectMapper;

    public JacksonWorkflowPayloadCodec() {
        this(defaultObjectMapper());
    }

    public JacksonWorkflowPayloadCodec(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public WorkflowPayloadCodec require(WorkflowDefinition<?, ?> definition, int payloadVersion) {
        Objects.requireNonNull(definition, "definition");
        if (payloadVersion != 1) {
            throw new WorkflowCodecUnavailableException(definition, payloadVersion);
        }
        return this;
    }

    @Override
    public String writeTyped(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize workflow payload", exception);
        }
    }

    @Override
    public <T> T readTyped(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            ObjectReader reader = objectMapper.readerFor(type);
            return reader.readValue(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize workflow payload", exception);
        }
    }

    @Override
    public <T> T convert(Object value, Class<T> type) {
        if (value == null) {
            return null;
        }
        return objectMapper.convertValue(value, type);
    }

    private static ObjectMapper defaultObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
