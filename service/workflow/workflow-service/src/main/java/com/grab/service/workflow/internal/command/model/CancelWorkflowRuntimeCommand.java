package com.grab.service.workflow.internal.command.model;

import com.grab.framework.cqrs.command.Command;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.CancelMessage;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.OperationResult;

public record CancelWorkflowRuntimeCommand(
        CancelMessage message
) implements Command<OperationResult> {
}
