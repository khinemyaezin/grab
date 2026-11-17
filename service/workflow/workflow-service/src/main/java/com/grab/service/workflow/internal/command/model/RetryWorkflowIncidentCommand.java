package com.grab.service.workflow.internal.command.model;

import com.grab.framework.cqrs.command.Command;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.OperationResult;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.RetryIncidentMessage;

public record RetryWorkflowIncidentCommand(
        RetryIncidentMessage message
) implements Command<OperationResult> {
}
