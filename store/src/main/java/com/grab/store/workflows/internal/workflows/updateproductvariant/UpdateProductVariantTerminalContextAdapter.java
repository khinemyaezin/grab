package com.grab.store.workflows.internal.workflows.updateproductvariant;

import com.grab.store.workflows.internal.service.WorkflowTerminalContextAdapter;
import com.grab.store.workflows.internal.service.WorkflowTerminalDetails;
import org.springframework.stereotype.Component;

@Component
public final class UpdateProductVariantTerminalContextAdapter
        implements WorkflowTerminalContextAdapter<UpdateProductVariantContext> {

    @Override
    public String workflowName() {
        return UpdateProductVariantWorkflowNames.WORKFLOW_NAME;
    }

    @Override
    public Class<UpdateProductVariantContext> contextType() {
        return UpdateProductVariantContext.class;
    }

    @Override
    public WorkflowTerminalDetails from(UpdateProductVariantContext context) {
        return new WorkflowTerminalDetails(
                context.createdBy(),
                context.scopeId(),
                context.isPartiallyApplied()
        );
    }
}
