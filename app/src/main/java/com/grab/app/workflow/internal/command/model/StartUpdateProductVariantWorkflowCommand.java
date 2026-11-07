package com.grab.app.workflow.internal.command.model;

import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantResult;
import com.grab.framework.cqrs.command.Command;

public record StartUpdateProductVariantWorkflowCommand(
        UpdateProductVariantMessage message
) implements Command<UpdateProductVariantResult> {
}
