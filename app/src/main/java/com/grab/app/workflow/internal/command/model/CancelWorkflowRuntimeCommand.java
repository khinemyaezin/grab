package com.grab.app.workflow.internal.command.model;

import com.grab.app.workflow.port.message.WorkflowRuntimeCommandPort;
import com.grab.framework.cqrs.command.Command;

public record CancelWorkflowRuntimeCommand(
        WorkflowRuntimeCommandPort.CancelMessage message
) implements Command<WorkflowRuntimeCommandPort.OperationResult> {
}
