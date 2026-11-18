package com.grab.workflow.infrastructure.serialization;

import com.grab.framework.workflow.WorkflowFailureClassification;
import com.grab.framework.workflow.WorkflowFingerprintValue;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowSignal;
import com.grab.framework.workflow.WorkflowSignalOutcome;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonWorkflowFingerprintTest {

    private final JacksonWorkflowFingerprint fingerprint = new JacksonWorkflowFingerprint();

    @Test
    void start_distinctListsWithDelimiters_producesDistinctHashes() {
        WorkflowFingerprintValue first = fingerprint.start("order", 1, 1, List.of("a", "b"));
        WorkflowFingerprintValue second = fingerprint.start("order", 1, 1, List.of("a,java.lang.String:b"));
        assertNotEquals(first, second);
        assertNotEquals(fingerprint.start("order", 1, 1, "Aa"), fingerprint.start("order", 1, 1, "BB"));
    }

    @Test
    void start_nestedMapKeysReordered_producesSameHash() {
        Map<String, Object> ascending = new LinkedHashMap<>();
        ascending.put("a", "quote: \" slash: \\");
        ascending.put("b", 2);
        Map<String, Object> descending = new LinkedHashMap<>();
        descending.put("b", 2);
        descending.put("a", "quote: \" slash: \\");
        WorkflowFingerprintValue first = fingerprint.start("order", 1, 1, Map.of("nested", ascending));
        WorkflowFingerprintValue second = fingerprint.start("order", 1, 1, Map.of("nested", descending));
        assertEquals(first, second);
    }

    @Test
    void signal_sameOutcomeAtDifferentTimes_changesOnlyTransportFingerprint() {
        WorkflowSignal first = signal("message-1", Instant.EPOCH);
        WorkflowSignal second = signal("message-2", Instant.EPOCH.plusSeconds(1));
        assertEquals(fingerprint.signal(first), fingerprint.signal(second));
        assertNotEquals(fingerprint.transport(first), fingerprint.transport(second));
    }

    @Test
    void start_unsupportedPayloads_rejectsInsteadOfCoercing() {
        assertThrows(IllegalArgumentException.class, () -> fingerprint.start("order", 1, 1, Set.of("a", "b")));
        assertThrows(IllegalArgumentException.class, () -> fingerprint.start("order", 1, 1, Map.of(12, "value")));
        assertThrows(IllegalArgumentException.class, () -> fingerprint.start("order", 1, 1, Double.NaN));
    }

    @Test
    void id_isFingerprintedAndRestoredAsScalar() {
        Id original = new CommonId("merchant-123");
        assertEquals(fingerprint.start("workflow", 1, 1, original),
                fingerprint.start("workflow", 1, 1, new CommonId("merchant-123")));
        JacksonWorkflowPayloadCodec codec = new JacksonWorkflowPayloadCodec();
        String json = codec.writeTyped(new IdPayload(original));
        IdPayload restored = codec.readTyped(json, IdPayload.class);
        assertEquals(original.getValue(), restored.merchantId().getValue());
        assertEquals("{\"merchantId\":\"merchant-123\"}", json);
    }

    private record IdPayload(Id merchantId) {
    }

    @Test
    void start_enumUsesDeclaredNameAndNestedSetIsRejected() {
        WorkflowFingerprintValue expected = fingerprint.start("order", 1, 1, "RESERVED");
        WorkflowFingerprintValue actual = fingerprint.start("order", 1, 1, ReservationStatus.RESERVED);
        assertEquals(expected, actual);
        UnsupportedPayload payload = new UnsupportedPayload(Set.of("b", "a"));
        assertThrows(IllegalArgumentException.class, () -> fingerprint.start("order", 1, 1, payload));
    }

    private record UnsupportedPayload(Set<String> values) {
    }

    private enum ReservationStatus {
        RESERVED;

        @Override
        public String toString() {
            return "custom";
        }
    }

    @Test
    void start_fingerprintIsStableInIndependentJvms() throws Exception {
        WorkflowFingerprintValue expected = fingerprint.start("order", 1, 1, Map.of("b", 2, "a", "value"));
        assertEquals(expected.digest(), fingerprintInChildJvm());
        assertEquals(expected.digest(), fingerprintInChildJvm());
    }

    private String fingerprintInChildJvm() throws Exception {
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        String mainClass = FingerprintProcess.class.getName();
        Process process = new ProcessBuilder(java.toString(), "-cp", classpath, mainClass)
                .redirectErrorStream(true)
                .start();
        try {
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Fingerprint process did not exit");
            }
            byte[] output = process.getInputStream().readAllBytes();
            String value = new String(output, StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), value);
            return value.strip();
        } finally {
            process.destroyForcibly();
        }
    }

    public static final class FingerprintProcess {
        public static void main(String[] args) {
            JacksonWorkflowFingerprint fingerprint = new JacksonWorkflowFingerprint();
            WorkflowFingerprintValue value = fingerprint.start("order", 1, 1, Map.of("b", 2, "a", "value"));
            System.out.println(value.digest());
        }
    }

    private WorkflowSignal signal(String messageId, Instant at) {
        return new WorkflowSignal(messageId, "execution", "line", "operation", "attempt", 1,
                WorkflowPhase.FORWARD, WorkflowSignalOutcome.SUCCEEDED, "reserved",
                WorkflowFailureClassification.UNCLASSIFIED, null, null, 1, at);
    }
}
