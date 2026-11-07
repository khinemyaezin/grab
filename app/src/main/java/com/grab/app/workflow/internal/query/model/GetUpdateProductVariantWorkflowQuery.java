package com.grab.app.workflow.internal.query.model;

import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantResult;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetUpdateProductVariantWorkflowQuery(
        Id workflowId
) implements Query<UpdateProductVariantResult> {
}

