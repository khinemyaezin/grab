package com.grab.framework.workflow;

import java.time.Instant;
import java.util.Objects;

public final class WorkflowRuntime {

    private final WorkflowDefinitionRegistry definitions;
    private final WorkflowTransitionEngine engine;
    private final WorkflowExecutionStore executionStore;
    private final WorkflowStartReceiptStore startReceiptStore;
    private final WorkflowSignalReceiptStore signalReceiptStore;
    private final WorkflowHistoryStore historyStore;
    private final WorkflowDispatchPublisher dispatchPublisher;
    private final WorkflowTimerScheduler timerScheduler;
    private final WorkflowFingerprint fingerprint;

    public WorkflowRuntime(
            WorkflowDefinitionRegistry definitions,
            WorkflowTransitionEngine engine,
            WorkflowExecutionStore executionStore,
            WorkflowStartReceiptStore startReceiptStore,
            WorkflowSignalReceiptStore signalReceiptStore,
            WorkflowHistoryStore historyStore,
            WorkflowDispatchPublisher dispatchPublisher,
            WorkflowTimerScheduler timerScheduler,
            WorkflowFingerprint fingerprint
    ) {
        this.definitions = Objects.requireNonNull(definitions, "definitions");
        this.engine = Objects.requireNonNull(engine, "engine");
        this.executionStore = Objects.requireNonNull(executionStore, "executionStore");
        this.startReceiptStore = Objects.requireNonNull(startReceiptStore, "startReceiptStore");
        this.signalReceiptStore = Objects.requireNonNull(signalReceiptStore, "signalReceiptStore");
        this.historyStore = Objects.requireNonNull(historyStore, "historyStore");
        this.dispatchPublisher = Objects.requireNonNull(dispatchPublisher, "dispatchPublisher");
        this.timerScheduler = Objects.requireNonNull(timerScheduler, "timerScheduler");
        this.fingerprint = Objects.requireNonNull(fingerprint, "fingerprint");
    }

    public RuntimeStartResult start(
            String definitionName,
            int definitionVersion,
            int payloadVersion,
            String callerScope,
            String idempotencyKey,
            Object input,
            String executionId,
            Instant now
    ) {
        Objects.requireNonNull(input, "input");
        WorkflowDefinition<?, ?> definition = definitions.require(definitionName, definitionVersion);
        WorkflowFingerprintValue fingerprintValue = fingerprint.start(definitionName, definitionVersion, payloadVersion, input);
        int fingerprintVersion = fingerprintValue.algorithmVersion();
        String inputFingerprint = fingerprintValue.digest();
        WorkflowStartReceipt existingReceipt = startReceiptStore.find(callerScope, idempotencyKey).orElse(null);
        if (existingReceipt != null) {
            if (existingReceipt.fingerprintVersion() != fingerprintVersion) {
                throw new WorkflowFingerprintUnavailableException("Start fingerprint version is unavailable: " + existingReceipt.fingerprintVersion());
            }
            if (!existingReceipt.inputFingerprint().equals(inputFingerprint)
                    || !existingReceipt.definitionName().equals(definitionName)
                    || existingReceipt.definitionVersion() != definitionVersion) {
                throw new WorkflowIdempotencyConflictException("Idempotency key was reused with different workflow input");
            }
            WorkflowStoredExecution existing = executionStore.find(existingReceipt.executionId(), definition)
                    .orElseThrow(() -> new IllegalStateException("Start receipt references a missing execution"));
            return new RuntimeStartResult(existing, true);
        }
        WorkflowTransition transition = startDefinition(definition, executionId, callerScope, payloadVersion, input, now);
        WorkflowStoredExecution stored = new WorkflowStoredExecution(
                callerScope,
                payloadVersion,
                idempotencyKey,
                transition.state(),
                now,
                now,
                0
        );
        WorkflowStoredExecution saved = executionStore.create(stored);
        recordTransition(saved, transition, now, "START", null, null);
        startReceiptStore.insert(new WorkflowStartReceipt(
                callerScope,
                idempotencyKey,
                definitionName,
                definitionVersion,
                fingerprintVersion,
                inputFingerprint,
                executionId,
                now
        ));
        return new RuntimeStartResult(saved, false);
    }

    public RuntimeSignalResult accept(String callerScope, WorkflowSignal signal, Instant now) {
        WorkflowExecutionMetadata metadata = metadata(signal.executionId());
        verifyScope(metadata, callerScope);
        if (signal.payloadVersion() != metadata.payloadVersion()) {
            throw new WorkflowCorrelationException("Signal payload version does not match execution");
        }
        WorkflowFingerprintValue transport = fingerprint.transport(signal);
        WorkflowFingerprintValue outcome = fingerprint.signal(signal);
        String transportFingerprint = transport.digest();
        String outcomeFingerprint = outcome.digest();
        int fingerprintVersion = transport.algorithmVersion();
        WorkflowSignalReceipt existingMessage = signalReceiptStore.findByMessageId(signal.messageId()).orElse(null);
        if (existingMessage != null) {
            if (existingMessage.fingerprintVersion() != fingerprintVersion) {
                throw new WorkflowFingerprintUnavailableException("Signal fingerprint version is unavailable: " + existingMessage.fingerprintVersion());
            }
            if (!existingMessage.payloadFingerprint().equals(transportFingerprint)) {
                throw new WorkflowIdempotencyConflictException("Signal message ID was reused with different content");
            }
            return new RuntimeSignalResult(metadata.executionId(), metadata.status(), true, SignalDisposition.DUPLICATE);
        }
        WorkflowSignalReceipt existingFingerprint = signalReceiptStore.findByFingerprint(
                signal.executionId(),
                signal.logicalOperationId(),
                outcomeFingerprint,
                fingerprintVersion
        ).orElse(null);
        if (existingFingerprint != null) {
            insertSignalReceipt(signal, transportFingerprint, outcomeFingerprint, fingerprintVersion, now);
            return new RuntimeSignalResult(metadata.executionId(), metadata.status(), true, SignalDisposition.DUPLICATE);
        }
        WorkflowStoredExecution stored = loadOrRecordIncident(metadata, now);
        if (stored == null) {
            return new RuntimeSignalResult(metadata.executionId(), metadata(metadata.executionId()).status(), false, SignalDisposition.REJECTED);
        }
        WorkflowDefinition<?, ?> definition = definitions.require(metadata.definitionName(), metadata.definitionVersion());
        WorkflowTransition transition = engine.accept(definition, stored.state(), signal, now);
        insertSignalReceipt(signal, transportFingerprint, outcomeFingerprint, fingerprintVersion, now);
        String eventType = "SIGNAL_" + transition.signalDisposition().name();
        WorkflowStoredExecution saved = persist(stored, transition, now, eventType, signal.nodeId());
        boolean duplicate = transition.signalDisposition() == SignalDisposition.DUPLICATE;
        return new RuntimeSignalResult(saved.state().executionId(), saved.state().status(), duplicate,
                transition.signalDisposition());
    }

    private void insertSignalReceipt(WorkflowSignal signal, String transportFingerprint, String outcomeFingerprint,
                                     int fingerprintVersion, Instant now) {
        WorkflowSignalReceipt receipt = new WorkflowSignalReceipt(
                signal.messageId(), signal.executionId(), signal.nodeId(), signal.logicalOperationId(),
                signal.attemptId(), signal.attempt(), signal.phase(), transportFingerprint, outcomeFingerprint,
                fingerprintVersion, signal.payloadVersion(), signal.outcome(), WorkflowSignalReceiptStatus.PROCESSED,
                now, now
        );
        signalReceiptStore.insert(receipt);
    }

    public RuntimeOperationResult cancel(String callerScope, String executionId, Instant now) {
        return cancel(callerScope, executionId, null, now);
    }

    public RuntimeOperationResult cancel(String callerScope, String executionId, String reason, Instant now) {
        WorkflowExecutionMetadata metadata = metadata(executionId);
        verifyScope(metadata, callerScope);
        WorkflowStoredExecution stored = loadOrRecordIncident(metadata, now);
        if (stored == null) {
            return new RuntimeOperationResult(metadata.executionId(), metadata(metadata.executionId()).status());
        }
        WorkflowDefinition<?, ?> definition = definitions.require(metadata.definitionName(), metadata.definitionVersion());
        WorkflowTransition transition = engine.cancel(definition, stored.state(), now);
        WorkflowStoredExecution saved = persist(stored, transition, now, "CANCEL", null, reason);
        return new RuntimeOperationResult(saved.state().executionId(), saved.state().status());
    }

    public RuntimeOperationResult onTimer(WorkflowTimerClaim claim, Instant now) {
        WorkflowExecutionMetadata metadata = metadata(claim.executionId());
        WorkflowStoredExecution stored = loadOrRecordIncident(metadata, now);
        if (stored == null) {
            return new RuntimeOperationResult(metadata.executionId(), ExecutionStatus.NEEDS_ATTENTION);
        }
        WorkflowDefinition<?, ?> definition = definitions.require(metadata.definitionName(), metadata.definitionVersion());
        WorkflowTransition transition = engine.onTimer(definition, stored.state(), claim.timer(), now);
        WorkflowStoredExecution saved = persist(stored, transition, now, "TIMER_" + claim.timer().purpose(), claim.timer().nodeId());
        return new RuntimeOperationResult(saved.state().executionId(), saved.state().status());
    }

    public RuntimeOperationResult retryIncident(String callerScope, String executionId, Instant now) {
        return retryIncident(callerScope, executionId, null, now);
    }

    public RuntimeOperationResult retryIncident(String callerScope, String executionId, String reason, Instant now) {
        WorkflowExecutionMetadata metadata = metadata(executionId);
        verifyScope(metadata, callerScope);
        WorkflowStoredExecution stored = loadOrRecordIncident(metadata, now);
        if (stored == null) {
            return new RuntimeOperationResult(metadata.executionId(), ExecutionStatus.NEEDS_ATTENTION);
        }
        WorkflowDefinition<?, ?> definition = definitions.require(metadata.definitionName(), metadata.definitionVersion());
        WorkflowTransition transition = engine.retryIncident(definition, stored.state(), now);
        WorkflowStoredExecution saved = persist(stored, transition, now, "RETRY_INCIDENT", null, reason);
        return new RuntimeOperationResult(saved.state().executionId(), saved.state().status());
    }

    private WorkflowTransition startDefinition(
            WorkflowDefinition<?, ?> definition,
            String executionId,
            String callerScope,
            int payloadVersion,
            Object input,
            Instant now
    ) {
        return startUnchecked(definition, executionId, callerScope, payloadVersion, input, now);
    }

    private <I, O> WorkflowTransition startUnchecked(
            WorkflowDefinition<I, O> definition,
            String executionId,
            String callerScope,
            int payloadVersion,
            Object input,
            Instant now
    ) {
        I typedInput = definition.inputType().cast(input);
        return engine.start(definition, executionId, callerScope, payloadVersion, typedInput, now);
    }

    private WorkflowStoredExecution persist(
            WorkflowStoredExecution previous,
            WorkflowTransition transition,
            Instant now,
            String eventType,
            String nodeId
    ) {
        return persist(previous, transition, now, eventType, nodeId, null);
    }

    private WorkflowStoredExecution persist(
            WorkflowStoredExecution previous,
            WorkflowTransition transition,
            Instant now,
            String eventType,
            String nodeId,
            String reason
    ) {
        if (transition.state().equals(previous.state())
                && transition.dispatches().isEmpty()
                && transition.timers().isEmpty()) {
            return previous;
        }
        return writeTransition(previous, transition, now, eventType, nodeId, reason);
    }

    private WorkflowStoredExecution writeTransition(WorkflowStoredExecution previous, WorkflowTransition transition,
                                                      Instant now, String eventType, String nodeId, String reason) {
        WorkflowStoredExecution next = new WorkflowStoredExecution(
                previous.callerScope(),
                previous.payloadVersion(),
                previous.idempotencyKey(),
                transition.state(),
                previous.createdAt(),
                now,
                previous.version()
        );
        WorkflowStoredExecution saved = executionStore.save(next);
        recordTransition(saved, transition, now, eventType, nodeId, reason);
        return saved;
    }

    private void recordTransition(WorkflowStoredExecution saved, WorkflowTransition transition, Instant now,
                                  String eventType, String nodeId, String reason) {
        appendHistory(saved.state().executionId(), eventType, nodeId, reason, now);
        dispatchPublisher.publish(saved.state().executionId(), transition.dispatches());
        timerScheduler.schedule(saved.state().executionId(), transition.timers());
    }

    private void appendHistory(String executionId, String eventType, String nodeId, String reason, Instant occurredAt) {
        long sequence = historyStore.nextSequence(executionId);
        historyStore.append(new WorkflowHistoryEntry(executionId, sequence, nodeId, eventType, reason, occurredAt));
    }

    private WorkflowExecutionMetadata metadata(String executionId) {
        return executionStore.findMetadata(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow execution was not found: " + executionId));
    }

    public ExecutionStatus recordUnavailable(String callerScope, String executionId, String explanation, Instant now) {
        WorkflowExecutionMetadata metadata = metadata(executionId);
        verifyScope(metadata, callerScope);
        return recordUnavailable(metadata, explanation, now);
    }

    private ExecutionStatus recordUnavailable(WorkflowExecutionMetadata metadata, String explanation, Instant now) {
        if (metadata.status() == ExecutionStatus.COMPLETED || metadata.status() == ExecutionStatus.CANCELLED
                || metadata.status() == ExecutionStatus.COMPENSATED || metadata.status() == ExecutionStatus.FAILED) {
            return metadata.status();
        }
        boolean changed = executionStore.recordUnavailable(metadata.executionId(), explanation, now);
        if (changed) {
            appendHistory(metadata.executionId(), "RECOVERY_REQUIRED", null, explanation, now);
        }
        return ExecutionStatus.NEEDS_ATTENTION;
    }

    private WorkflowStoredExecution loadOrRecordIncident(WorkflowExecutionMetadata metadata, Instant now) {
        try {
            return load(metadata);
        } catch (WorkflowDefinitionUnavailableException | WorkflowCodecUnavailableException
                 | WorkflowFingerprintUnavailableException exception) {
            recordUnavailable(metadata, exception.getMessage(), now);
            return null;
        }
    }

    private WorkflowStoredExecution load(WorkflowExecutionMetadata metadata) {
        WorkflowDefinition<?, ?> definition = definitions.require(metadata.definitionName(), metadata.definitionVersion());
        return executionStore.find(metadata.executionId(), definition)
                .orElseThrow(() -> new IllegalArgumentException("Workflow execution was not found: " + metadata.executionId()));
    }

    private static void verifyScope(WorkflowExecutionMetadata metadata, String callerScope) {
        if (!metadata.callerScope().equals(callerScope)) {
            throw new SecurityException("Workflow caller scope does not match execution scope");
        }
    }

    public record RuntimeStartResult(WorkflowStoredExecution execution, boolean duplicate) {
    }

    public record RuntimeSignalResult(
            String executionId,
            ExecutionStatus status,
            boolean duplicate,
            SignalDisposition disposition
    ) {
    }

    public record RuntimeOperationResult(String executionId, ExecutionStatus status) {
    }
}
