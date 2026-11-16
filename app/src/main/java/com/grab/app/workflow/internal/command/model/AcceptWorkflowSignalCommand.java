package com.grab.app.workflow.internal.command.model;

import com.grab.app.workflow.port.message.WorkflowRuntimeSignalPort;
import com.grab.framework.cqrs.command.Command;

public record AcceptWorkflowSignalCommand(
        WorkflowRuntimeSignalPort.SignalMessage message
) implements Command<WorkflowRuntimeSignalPort.SignalResult> {
}
