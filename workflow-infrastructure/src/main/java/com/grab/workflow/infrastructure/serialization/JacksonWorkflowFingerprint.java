package com.grab.workflow.infrastructure.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.grab.framework.workflow.WorkflowFingerprint;
import com.grab.framework.workflow.WorkflowFingerprintValue;
import com.grab.framework.workflow.WorkflowSignal;
import com.grab.framework.id.Id;

import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAccessor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class JacksonWorkflowFingerprint implements WorkflowFingerprint {

    private static final int ALGORITHM_VERSION = 2;
    private static final HexFormat HEX = HexFormat.of();
    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .addModule(new WorkflowPayloadJacksonModule())
            .disable(MapperFeature.USE_ANNOTATIONS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .build();

    @Override
    public int algorithmVersion() {
        return ALGORITHM_VERSION;
    }

    @Override
    public WorkflowFingerprintValue start(String definitionName, int definitionVersion, int payloadVersion, Object input) {
        JsonNode payload = payload(input);
        StartEnvelope envelope = new StartEnvelope(definitionName, definitionVersion, payloadVersion, payload);
        return digest(envelope);
    }

    @Override
    public WorkflowFingerprintValue signal(WorkflowSignal signal) {
        SignalEnvelope envelope = envelope(signal);
        return digest(envelope);
    }

    @Override
    public WorkflowFingerprintValue transport(WorkflowSignal signal) {
        SignalEnvelope outcome = envelope(signal);
        TransportEnvelope envelope = new TransportEnvelope(outcome, signal.occurredAt());
        return digest(envelope);
    }

    private SignalEnvelope envelope(WorkflowSignal signal) {
        JsonNode payload = payload(signal.payload());
        return new SignalEnvelope(signal.executionId(), signal.nodeId(), signal.logicalOperationId(),
                signal.attemptId(), signal.attempt(), signal.phase().name(), signal.outcome().name(),
                signal.failureClassification().name(), signal.payloadVersion(), payload,
                signal.errorCode(), signal.error());
    }

    private JsonNode payload(Object value) {
        Map<Object, Boolean> visited = new IdentityHashMap<>();
        Set<Object> active = Collections.newSetFromMap(visited);
        validate(value, active);
        return mapper.valueToTree(value);
    }

    private static void validate(Object value, Set<Object> active) {
        if (value instanceof Set<?> || value instanceof Double number && !Double.isFinite(number)
                || value instanceof Float number && !Float.isFinite(number)) {
            throw new IllegalArgumentException("Unsupported workflow fingerprint payload");
        }
        if (value == null) {
            return;
        }
        if (!active.add(value)) {
            throw new IllegalArgumentException("Workflow fingerprint payload contains a cycle");
        }
        try {
            validateChildren(value, active);
        } finally {
            active.remove(value);
        }
    }

    private static void validateChildren(Object value, Set<Object> active) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String)) {
                    throw new IllegalArgumentException("Workflow fingerprint maps require string keys");
                }
                validate(entry.getValue(), active);
            }
        } else if (value instanceof Iterable<?> iterable) {
            if (!(value instanceof List<?>)) {
                throw new IllegalArgumentException("Workflow fingerprint collections require a defined list order");
            }
            for (Object element : iterable) {
                validate(element, active);
            }
        } else if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                Object element = Array.get(value, index);
                validate(element, active);
            }
        } else if (value.getClass().isRecord()) {
            for (RecordComponent component : value.getClass().getRecordComponents()) {
                Method accessor = component.getAccessor();
                try {
                    if (!accessor.canAccess(value) && !accessor.trySetAccessible()) {
                        throw new IllegalArgumentException("Cannot validate workflow fingerprint record");
                    }
                    Object property = accessor.invoke(value);
                    validate(property, active);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalArgumentException("Cannot validate workflow fingerprint record", exception);
                }
            }
        } else if (!(value instanceof String || value instanceof Boolean || value instanceof Character
                || value instanceof Enum<?> || value instanceof Byte || value instanceof Short
                || value instanceof Integer || value instanceof Long || value instanceof Float
                || value instanceof Double || value instanceof BigInteger || value instanceof BigDecimal
                || value instanceof TemporalAccessor || value instanceof Duration || value instanceof Period
                || value instanceof Id
                || value instanceof UUID)) {
            throw new IllegalArgumentException("Unsupported workflow fingerprint payload type: " + value.getClass().getName());
        }
    }

    private JsonNode ordered(JsonNode node) {
        if (node instanceof ObjectNode object) {
            Map<String, JsonNode> properties = new TreeMap<>();
            object.fields().forEachRemaining(entry -> properties.put(entry.getKey(), entry.getValue()));
            ObjectNode sorted = mapper.createObjectNode();
            properties.forEach((key, value) -> {
                JsonNode child = ordered(value);
                sorted.set(key, child);
            });
            return sorted;
        }
        if (node instanceof ArrayNode array) {
            ArrayNode sortedChildren = mapper.createArrayNode();
            for (JsonNode value : array) {
                JsonNode child = ordered(value);
                sortedChildren.add(child);
            }
            return sortedChildren;
        }
        return node;
    }

    private WorkflowFingerprintValue digest(Object envelope) {
        try {
            JsonNode tree = mapper.valueToTree(envelope);
            JsonNode canonical = ordered(tree);
            byte[] content = mapper.writeValueAsBytes(canonical);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(content);
            String digest = HEX.formatHex(hash);
            return new WorkflowFingerprintValue(ALGORITHM_VERSION, digest);
        } catch (JsonProcessingException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Could not fingerprint workflow content", exception);
        }
    }

    private record StartEnvelope(String definitionName, int definitionVersion, int payloadVersion, JsonNode payload) {
    }

    private record SignalEnvelope(String executionId, String nodeId, String logicalOperationId, String attemptId,
                                  int attempt, String phase, String outcome, String failureClassification,
                                  int payloadVersion, JsonNode payload, String errorCode, String error) {
    }

    private record TransportEnvelope(SignalEnvelope outcome, Instant occurredAt) {
    }
}
