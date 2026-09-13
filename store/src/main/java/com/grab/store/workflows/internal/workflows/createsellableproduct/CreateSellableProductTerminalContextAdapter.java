package com.grab.store.workflows.internal.workflows.createsellableproduct;

import com.grab.store.workflows.internal.service.WorkflowTerminalContextAdapter;
import com.grab.store.workflows.internal.service.WorkflowTerminalDetails;
import org.springframework.stereotype.Component;

@Component
public final class CreateSellableProductTerminalContextAdapter
        implements WorkflowTerminalContextAdapter<CreateSellableProductContext> {

    @Override
    public String workflowName() {
        return CreateSellableProductWorkflowNames.WORKFLOW_NAME;
    }

    @Override
    public Class<CreateSellableProductContext> contextType() {
        return CreateSellableProductContext.class;
    }

    @Override
    public WorkflowTerminalDetails from(CreateSellableProductContext context) {
        return new WorkflowTerminalDetails(
                context.createdBy(),
                context.scopeId(),
                !context.inventoryItems().isEmpty()
        );
    }
}
