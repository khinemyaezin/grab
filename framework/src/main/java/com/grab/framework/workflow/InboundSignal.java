package com.grab.framework.workflow;

import com.grab.framework.domain.Event;

import java.util.Objects;
import java.util.Optional;

public record InboundSignal(
        SignalType type,
        String workflowId,
        CorrelationKey correlationKey,
        String stepHint,
        Event event,
        String dedupKey
) {

    public InboundSignal {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(dedupKey, "dedupKey");
        if (dedupKey.isBlank()) {
            throw new IllegalArgumentException("dedupKey must not be blank");
        }
        if ((workflowId == null || workflowId.isBlank()) && correlationKey == null) {
            throw new IllegalArgumentException("workflowId or correlationKey is required");
        }
    }

    public Optional<String> workflowIdOptional() {
        return workflowId == null || workflowId.isBlank() ? Optional.empty() : Optional.of(workflowId);
    }

    public Optional<CorrelationKey> correlationKeyOptional() {
        return Optional.ofNullable(correlationKey);
    }

    public Optional<String> stepHintOptional() {
        return stepHint == null || stepHint.isBlank() ? Optional.empty() : Optional.of(stepHint);
    }

    public static InboundSignal completion(String workflowId, String stepHint, Event event, String dedupKey) {
        return new InboundSignal(SignalType.COMPLETION, workflowId, null, stepHint, event, dedupKey);
    }

    public static InboundSignal failure(String workflowId, String stepHint, Event event, String dedupKey) {
        return new InboundSignal(SignalType.FAILURE, workflowId, null, stepHint, event, dedupKey);
    }

    public static InboundSignal compensationAck(String workflowId, Event event, String dedupKey) {
        return new InboundSignal(SignalType.COMPENSATION_ACK, workflowId, null, null, event, dedupKey);
    }

    public static InboundSignal correlated(CorrelationKey correlationKey, Event event, String dedupKey) {
        return new InboundSignal(SignalType.COMPLETION, null, correlationKey, null, event, dedupKey);
    }

    public static InboundSignal of(WorkflowSignalEvent event) {
        Objects.requireNonNull(event, "event");
        return new InboundSignal(
                event.signalType(),
                event.signalWorkflowId(),
                event.signalCorrelation(),
                null,
                event,
                event.signalDedupKey()
        );
    }
}
