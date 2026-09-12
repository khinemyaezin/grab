package com.grab.store.workflows.internal.service;

import com.grab.framework.workflow.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkflowSweeper {

    private final WorkflowEngine workflowEngine;

    @Scheduled(fixedDelayString = "${workflows.sweeper.fixed-delay-ms:30000}")
    public void sweep() {
        workflowEngine.sweep();
    }
}
