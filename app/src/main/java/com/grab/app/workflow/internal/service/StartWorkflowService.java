package com.grab.app.workflow.internal.service;

import com.grab.app.workflow.internal.port.inbound.StartWorkflowUseCase;
import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowDefinition;
import com.grab.framework.workflow.WorkflowDefinitionRegistry;
import com.grab.framework.workflow.WorkflowRuntime;
import com.grab.framework.workflow.WorkflowPayloadCodec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class StartWorkflowService implements StartWorkflowUseCase {

    private final WorkflowRuntime runtime;
    private final WorkflowDefinitionRegistry definitions;
    private final WorkflowPayloadCodec payloadCodec;
    private final IdGenerator idGenerator;
    private final Clock clock;
    private final boolean startsEnabled;

    public StartWorkflowService(
            WorkflowRuntime runtime,
            WorkflowDefinitionRegistry definitions,
            WorkflowPayloadCodec payloadCodec,
            IdGenerator idGenerator,
            @Qualifier("workflowClock") Clock clock,
            @Value("${workflow.starts-enabled:false}") boolean startsEnabled
    ) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.definitions = Objects.requireNonNull(definitions, "definitions");
        this.payloadCodec = Objects.requireNonNull(payloadCodec, "payloadCodec");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.startsEnabled = startsEnabled;
    }

    @Override
    public WorkflowRuntimeCommandPort.StartResult execute(WorkflowRuntimeCommandPort.StartMessage message) {
        if (!startsEnabled) {
            throw new IllegalStateException("Workflow starts are disabled");
        }
        WorkflowDefinition<?, ?> definition = definitions.require(message.definitionName(), message.definitionVersion());
        WorkflowPayloadCodec codec = payloadCodec.require(definition, message.payloadVersion());
        Object input = codec.readTyped(message.inputJson(), definition.inputType());
        String executionId = idGenerator.generateId().getValue();
        Instant now = clock.instant();
        WorkflowRuntime.RuntimeStartResult result = runtime.start(
                message.definitionName(),
                message.definitionVersion(),
                message.payloadVersion(),
                message.callerScope(),
                message.idempotencyKey(),
                input,
                executionId,
                now
        );
        return new WorkflowRuntimeCommandPort.StartResult(
                new CommonId(result.execution().state().executionId()),
                result.execution().state().workflowName(),
                result.execution().state().definitionVersion(),
                result.execution().state().status().name(),
                result.execution().createdAt()
        );
    }
}
