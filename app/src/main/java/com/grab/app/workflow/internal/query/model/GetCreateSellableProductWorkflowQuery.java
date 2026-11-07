package com.grab.app.workflow.internal.query.model;

import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductResult;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetCreateSellableProductWorkflowQuery(
        Id workflowId
) implements Query<CreateSellableProductResult> {
}

