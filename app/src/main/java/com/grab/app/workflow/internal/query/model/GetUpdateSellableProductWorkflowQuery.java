package com.grab.app.workflow.internal.query.model;

import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductResult;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetUpdateSellableProductWorkflowQuery(
        Id workflowId
) implements Query<UpdateSellableProductResult> {
}

