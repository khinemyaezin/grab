package com.grab.app.workflow.internal.service;

import com.grab.app.workflow.internal.port.inbound.WorkflowRuntimeSignalUseCase;
import com.grab.app.workflow.port.message.WorkflowRuntimeSignalPort;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowDefinitionRegistry;
import com.grab.framework.workflow.WorkflowCorrelationException;
import com.grab.framework.workflow.WorkflowExecutionMetadata;
import com.grab.framework.workflow.WorkflowExecutionStore;
import com.grab.framework.workflow.WorkflowPayloadCodec;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowRuntime;
import com.grab.framework.workflow.WorkflowSignal;
import com.grab.framework.workflow.WorkflowSignalOutcome;
import com.grab.framework.workflow.WorkflowUnit;
import com.grab.framework.workflow.WorkflowDefinitionUnavailableException;
import com.grab.framework.workflow.WorkflowCodecUnavailableException;
import com.grab.framework.workflow.ExecutionStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public final class WorkflowRuntimeSignalService implements WorkflowRuntimeSignalUseCase {

    private final WorkflowRuntime runtime;
    private final WorkflowDefinitionRegistry definitions;
    private final WorkflowExecutionStore executionStore;
    private final WorkflowPayloadCodec payloadCodec;
    private final Clock clock;

    public WorkflowRuntimeSignalService(
            WorkflowRuntime runtime,
            WorkflowDefinitionRegistry definitions,
            WorkflowExecutionStore executionStore,
            WorkflowPayloadCodec payloadCodec,
            @Qualifier("workflowClock") Clock clock
    ) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.definitions = Objects.requireNonNull(definitions, "definitions");
        this.executionStore = Objects.requireNonNull(executionStore, "executionStore");
        this.payloadCodec = Objects.requireNonNull(payloadCodec, "payloadCodec");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public WorkflowRuntimeSignalPort.SignalResult accept(WorkflowRuntimeSignalPort.SignalMessage message) {
        String executionId = message.executionId().getValue();
        WorkflowExecutionMetadata metadata = executionStore.findMetadata(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow execution was not found: " + executionId));
        if (!metadata.callerScope().equals(message.callerScope())) {
            throw new SecurityException("Workflow caller scope does not match execution scope");
        }
        if (message.payloadVersion() != metadata.payloadVersion()) {
            throw new WorkflowCorrelationException("Signal payload version does not match the pinned execution version");
        }
        Instant processingTime = clock.instant();
        Object payload;
        try {
            WorkflowDefinition<?, ?> definition = definitions.require(metadata.definitionName(), metadata.definitionVersion());
            WorkflowPayloadCodec codec = payloadCodec.require(definition, message.payloadVersion());
            payload = readPayload(message, definition, codec);
        } catch (WorkflowDefinitionUnavailableException | WorkflowCodecUnavailableException exception) {
            ExecutionStatus status = runtime.recordUnavailable(message.callerScope(), executionId,
                    exception.getMessage(), processingTime);
            return new WorkflowRuntimeSignalPort.SignalResult(message.executionId(), status.name(), false);
        }
        WorkflowSignal signal = new WorkflowSignal(
                message.messageId(), executionId, message.nodeId(), message.logicalOperationId(), message.attemptId(),
                message.attempt(), message.phase(), WorkflowSignalOutcome.valueOf(message.outcome().name()), payload,
                message.failureClassification(), message.errorCode(), message.errorMessage(), message.payloadVersion(),
                message.occurredAt()
        );
        WorkflowRuntime.RuntimeSignalResult result = runtime.accept(message.callerScope(), signal, processingTime);
        return new WorkflowRuntimeSignalPort.SignalResult(new CommonId(result.executionId()), result.status().name(), result.duplicate());
    }

    private Object readPayload(WorkflowRuntimeSignalPort.SignalMessage message, WorkflowDefinition<?, ?> definition, WorkflowPayloadCodec codec) {
        if (message.phase() == WorkflowPhase.COMPENSATION
                && message.outcome() == WorkflowRuntimeSignalPort.SignalOutcome.SUCCEEDED) {
            return codec.readTyped(message.payloadJson(), WorkflowUnit.class);
        }
        if (message.outcome() != WorkflowRuntimeSignalPort.SignalOutcome.SUCCEEDED) {
            if (message.payloadJson() == null || message.payloadJson().isBlank()) {
                return null;
            }
            return codec.readTyped(message.payloadJson(), Object.class);
        }
        if (!definition.nodes().containsKey(message.nodeId())) {
            throw new WorkflowCorrelationException("Signal node is not part of the pinned definition");
        }
        Class<?> payloadType = definition.nodes().get(message.nodeId()).outputType();
        if ((message.payloadJson() == null || message.payloadJson().isBlank()) && payloadType == WorkflowUnit.class) {
            return WorkflowUnit.INSTANCE;
        }
        if (message.payloadJson() == null || message.payloadJson().isBlank()) {
            return null;
        }
        return codec.readTyped(message.payloadJson(), payloadType);
    }

}
