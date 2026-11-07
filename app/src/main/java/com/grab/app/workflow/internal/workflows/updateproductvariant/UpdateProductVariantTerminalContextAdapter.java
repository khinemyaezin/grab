package com.grab.app.workflow.internal.workflows.updateproductvariant;

import com.grab.app.workflow.internal.service.WorkflowTerminalContextAdapter;
import com.grab.app.workflow.internal.service.WorkflowTerminalDetails;
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
