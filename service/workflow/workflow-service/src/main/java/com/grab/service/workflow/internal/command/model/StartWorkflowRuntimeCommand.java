package com.grab.service.workflow.internal.command.model;

import com.grab.framework.cqrs.command.Command;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.StartMessage;
import com.grab.service.contract.message.workflow.WorkflowRuntimeCommandPort.StartResult;

public record StartWorkflowRuntimeCommand(
        StartMessage message
) implements Command<StartResult> {
}
