package com.grab.framework.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.UUID;

public final class OutboxEventHeaders {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String EVENT_ID_KEY = "eventId";
    public static final String CONTENT_TYPE_KEY = "contentType";
    public static final String JSON_CONTENT_TYPE = "application/json";

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private OutboxEventHeaders() {
    }

    public static String mergeTraceId(String headersJson, String traceId) {
        return merge(headersJson, TRACE_ID_KEY, traceId);
    }

    public static String extractTraceId(String headersJson) {
        return extract(headersJson, TRACE_ID_KEY);
    }

    public static String mergeEventId(String headersJson, UUID eventId) {
        String value = eventId == null ? null : eventId.toString();
        return merge(headersJson, EVENT_ID_KEY, value);
    }

    public static UUID extractEventId(String headersJson) {
        String value = extract(headersJson, EVENT_ID_KEY);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String merge(String headersJson, String key, String value) {
        if (value == null || value.isBlank()) {
            return headersJson == null ? "{}" : headersJson;
        }

        try {
            ObjectNode node = parseObject(headersJson);
            node.put(key, value);
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to merge " + key + " into outbox headers", exception);
        }
    }

    private static String extract(String headersJson, String key) {
        try {
            ObjectNode node = parseObject(headersJson);
            JsonNode value = node.get(key);
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
