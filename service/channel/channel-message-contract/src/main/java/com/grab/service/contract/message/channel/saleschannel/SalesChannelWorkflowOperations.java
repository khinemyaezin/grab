package com.grab.service.contract.message.channel.saleschannel;

import com.grab.service.contract.event.channel.saleschannel.SalesChannelWorkflowPayloads.PublicationChannels;
import com.grab.framework.workflow.WorkflowOperation;
import com.grab.framework.workflow.WorkflowUnit;

public final class SalesChannelWorkflowOperations {

    private SalesChannelWorkflowOperations() {
    }

    public static final WorkflowOperation<PublicationChannels, WorkflowUnit> ASSERT_TARGETS =
            WorkflowOperation.assertion("sales.assert-publication-targets", PublicationChannels.class, WorkflowUnit.class);
}
