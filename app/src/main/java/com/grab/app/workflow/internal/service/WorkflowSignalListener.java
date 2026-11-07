package com.grab.app.workflow.internal.service;

import com.grab.framework.workflow.InboundSignal;
import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowSignalEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WorkflowSignalListener {

    private final WorkflowEngine workflowEngine;

    public WorkflowSignalListener(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @EventListener
    public void onWorkflowSignal(WorkflowSignalEvent event) {
        workflowEngine.onSignal(InboundSignal.of(event));
    }
}

