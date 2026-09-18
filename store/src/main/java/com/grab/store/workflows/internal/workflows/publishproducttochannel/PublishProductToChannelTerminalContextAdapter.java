package com.grab.store.workflows.internal.workflows.publishproducttochannel;

import com.grab.store.workflows.internal.service.WorkflowTerminalContextAdapter;
import com.grab.store.workflows.internal.service.WorkflowTerminalDetails;
import org.springframework.stereotype.Component;

@Component
public final class PublishProductToChannelTerminalContextAdapter
        implements WorkflowTerminalContextAdapter<PublishProductToChannelContext> {

    @Override
    public String workflowName() {
        return PublishProductToChannelWorkflowNames.WORKFLOW_NAME;
    }

    @Override
    public Class<PublishProductToChannelContext> contextType() {
        return PublishProductToChannelContext.class;
    }

    @Override
    public WorkflowTerminalDetails from(PublishProductToChannelContext context) {
        return new WorkflowTerminalDetails(
                context.createdBy(),
                context.scopeId(),
                context.publicationWritten() && !context.publicationCompensated()
        );
    }
}
