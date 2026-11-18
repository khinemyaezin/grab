package com.grab.app.workflow.internal.adapter.inbound.event;

import com.grab.app.workflow.port.message.WorkflowRuntimeSignalPort;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowTaskOutcomeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class WorkflowTaskOutcomeEventListener {

    private final WorkflowRuntimeSignalPort signalPort;

    public WorkflowTaskOutcomeEventListener(WorkflowRuntimeSignalPort signalPort) {
        this.signalPort = Objects.requireNonNull(signalPort, "signalPort");
    }

    @EventListener
    public void onTaskOutcome(WorkflowTaskOutcomeEvent event) {
        WorkflowRuntimeSignalPort.SignalMessage message = new WorkflowRuntimeSignalPort.SignalMessage(
                event.messageId(),
                new CommonId(event.executionId()),
                event.nodeId(),
                event.logicalOperationId(),
                event.attemptId(),
                event.attempt(),
                event.phase(),
                WorkflowRuntimeSignalPort.SignalOutcome.valueOf(event.outcome().name()),
                event.payloadJson(),
                event.payloadVersion(),
                event.callerScope(),
                event.failureClassification(),
                event.errorCode(),
                event.errorMessage(),
                event.occurredAt()
        );
        signalPort.accept(message);
    }
}
