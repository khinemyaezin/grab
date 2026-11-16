package com.grab.app.workflow.internal.command.model;

import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort;
import com.grab.framework.cqrs.command.Command;

public record RetryWorkflowIncidentCommand(
        WorkflowRuntimeCommandPort.RetryIncidentMessage message
) implements Command<WorkflowRuntimeCommandPort.OperationResult> {
}
