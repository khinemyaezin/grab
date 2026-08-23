package com.grab.framework.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public final class OutboxEventHeaders {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String CONTENT_TYPE_KEY = "contentType";
    public static final String JSON_CONTENT_TYPE = "application/json";

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private OutboxEventHeaders() {
    }

    public static String mergeTraceId(String headersJson, String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return headersJson == null ? "{}" : headersJson;
        }

        try {
            ObjectNode node = parseObject(headersJson);
            node.put(TRACE_ID_KEY, traceId);
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to merge traceId into outbox headers", exception);
        }
    }

    public static String extractTraceId(String headersJson) {
        try {
            ObjectNode node = parseObject(headersJson);
            JsonNode value = node.get(TRACE_ID_KEY);
            if (value == null || !value.isTextual()) {
                return null;
            }
            String text = value.asText();
            return text.isBlank() ? null : text;
        } catch (IOException exception) {
            return null;
        }
    }

    private static ObjectNode parseObject(String headersJson) throws JsonProcessingException {
        if (headersJson == null || headersJson.isBlank()) {
            return MAPPER.createObjectNode();
        }

        JsonNode root = MAPPER.readTree(headersJson);
        if (root != null && root.isObject()) {
            return (ObjectNode) root;
        }
        return MAPPER.createObjectNode();
    }
}
