package com.grab.app.workflows.internal.adapter.inbound.event;

import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowSignalEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkflowSignalInbox {

    private final WorkflowEngine workflowEngine;

    @EventListener
    public void onWorkflowSignal(WorkflowSignalEvent event) {
        workflowEngine.onSignal(InboundSignal.of(event));
    }
}
