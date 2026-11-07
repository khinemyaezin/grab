package com.grab.app.workflow.internal.command.model;

import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult;
import com.grab.framework.cqrs.command.Command;

public record StartUpdateSellableProductWorkflowCommand(
        UpdateSellableProductMessage message
) implements Command<UpdateSellableProductResult> {
}
