package com.grab.service.workflow.internal.command.model;

import com.grab.framework.cqrs.command.Command;
import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort.SignalMessage;
import com.grab.service.contract.message.workflow.WorkflowRuntimeSignalPort.SignalResult;

public record AcceptWorkflowSignalCommand(
        SignalMessage message
) implements Command<SignalResult> {
}
