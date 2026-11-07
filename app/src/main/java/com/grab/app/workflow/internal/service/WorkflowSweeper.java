package com.grab.app.workflow.internal.service;

import com.grab.framework.workflow.WorkflowEngine;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WorkflowSweeper {

    private final WorkflowEngine workflowEngine;

    public WorkflowSweeper(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }

    @Scheduled(fixedDelayString = "${workflows.sweeper.fixed-delay-ms:30000}")
    public void sweep() {
        workflowEngine.sweep();
    }
}

