package com.grab.app.workflow.internal.service;

import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.framework.workflow.WorkflowEngine;
import com.grab.scheduling.infrastructure.ClusterSingletonJob;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Class B singleton for now; instance {@code @Version} already prevents double-advance.
 * Migrating to a SKIP LOCKED competing worker (class A) is tracked in the scheduling plan (3.5).
 */
@Component
public class WorkflowSweeper {

    private static final String SWEEP_JOB_NAME = "workflows.sweeper";
    private static final SingletonJob SWEEP_JOB = new SingletonJob(
            SWEEP_JOB_NAME, Duration.ofMinutes(5), Duration.ofSeconds(20));

    private final WorkflowEngine workflowEngine;
    private final SingletonJobExecutor singletonJobExecutor;

    public WorkflowSweeper(
            WorkflowEngine workflowEngine,
            @Qualifier("workflowsSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor
    ) {
        this.workflowEngine = workflowEngine;
        this.singletonJobExecutor = singletonJobExecutor;
    }

    @ClusterSingletonJob(SWEEP_JOB_NAME)
    @Scheduled(fixedDelayString = "${workflows.sweeper.fixed-delay-ms:30000}")
    public void sweep() {
        singletonJobExecutor.execute(SWEEP_JOB, workflowEngine::sweep);
    }
}
