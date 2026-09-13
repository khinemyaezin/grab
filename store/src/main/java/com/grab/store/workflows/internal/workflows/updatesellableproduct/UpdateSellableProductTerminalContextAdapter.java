package com.grab.store.workflows.internal.workflows.updatesellableproduct;

import com.grab.store.workflows.internal.service.WorkflowTerminalContextAdapter;
import com.grab.store.workflows.internal.service.WorkflowTerminalDetails;
import org.springframework.stereotype.Component;

@Component
public final class UpdateSellableProductTerminalContextAdapter
        implements WorkflowTerminalContextAdapter<UpdateSellableProductContext> {

    @Override
    public String workflowName() {
        return UpdateSellableProductWorkflowNames.WORKFLOW_NAME;
    }

    @Override
    public Class<UpdateSellableProductContext> contextType() {
        return UpdateSellableProductContext.class;
    }

    @Override
    public WorkflowTerminalDetails from(UpdateSellableProductContext context) {
        return new WorkflowTerminalDetails(
                context.createdBy(),
                context.scopeId(),
                context.isPartiallyApplied()
        );
    }
}
