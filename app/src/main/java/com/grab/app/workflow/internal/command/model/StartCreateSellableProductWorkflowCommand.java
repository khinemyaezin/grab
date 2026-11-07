package com.grab.app.workflow.internal.command.model;

import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductResult;
import com.grab.framework.cqrs.command.Command;

public record StartCreateSellableProductWorkflowCommand(
        CreateSellableProductMessage message
) implements Command<CreateSellableProductResult> {
}
