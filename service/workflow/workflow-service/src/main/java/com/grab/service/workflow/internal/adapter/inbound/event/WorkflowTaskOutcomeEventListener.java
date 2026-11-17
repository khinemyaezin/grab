package com.grab.service.workflow.internal.adapter.inbound.event;

import com.grab.framework.id.impl.CommonId;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.workflow.WorkflowTaskOutcomeEvent;
import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;
import java.util.Objects;

@Component
public class WorkflowTaskOutcomeEventListener {

    private static final Logger log = Loggers.getLogger(WorkflowTaskOutcomeEventListener.class);

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
        try {
            signalPort.accept(message);
        } catch (RemoteException exception) {
            log.error("Failed to route task outcome event {} to signal port", event.messageId(), exception);
            throw new IllegalStateException("Failed to accept workflow task outcome: " + exception.getMessage(), exception);
        }
    }
}
