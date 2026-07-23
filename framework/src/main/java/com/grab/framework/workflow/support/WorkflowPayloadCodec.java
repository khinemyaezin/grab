package com.grab.framework.workflow.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grab.framework.workflow.WorkflowCheckpoint;
import com.grab.framework.workflow.WorkflowContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WorkflowPayloadCodec {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<Map<String, Object>>> CHECKPOINT_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public WorkflowPayloadCodec() {
        this(defaultObjectMapper());
    }

    public WorkflowPayloadCodec(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public String writeContext(WorkflowContext context) {
        try {
            return objectMapper.writeValueAsString(context.attributes());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize workflow context", exception);
        }
    }

    public void applyContext(WorkflowContext context, String contextJson) {
        if (contextJson == null || contextJson.isBlank()) {
            return;
        }
        try {
            Map<String, Object> attributes = objectMapper.readValue(contextJson, MAP_TYPE);
            context.replaceAttributes(attributes);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize workflow context", exception);
        }
    }

    public String writeCheckpoints(List<WorkflowCheckpoint> checkpoints) {
        List<Map<String, Object>> payload = checkpoints.stream()
                .map(checkpoint -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("stepName", checkpoint.stepName());
                    entry.put("output", checkpoint.output());
                    return entry;
                })
                .toList();
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize workflow checkpoints", exception);
        }
    }

    public List<WorkflowCheckpoint> readCheckpoints(String checkpointJson) {
        if (checkpointJson == null || checkpointJson.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> payload = objectMapper.readValue(checkpointJson, CHECKPOINT_LIST_TYPE);
            return payload.stream()
                    .map(entry -> new WorkflowCheckpoint(
                            String.valueOf(entry.get("stepName")),
                            entry.get("output")
                    ))
                    .toList();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize workflow checkpoints", exception);
        }
    }

    private static ObjectMapper defaultObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
