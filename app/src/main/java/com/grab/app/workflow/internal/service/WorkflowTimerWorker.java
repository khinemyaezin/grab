package com.grab.app.workflow.internal.service;

import com.grab.app.workflow.internal.port.inbound.WorkflowTimerProcessingPort;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.framework.workflow.WorkflowTimerClaim;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(name = "workflow.workers-enabled", havingValue = "true")
public class WorkflowTimerWorker {

    private static final Logger log = Loggers.getLogger(WorkflowTimerWorker.class);
    private static final int MAX_TIMERS_PER_POLL = 32;

    private final WorkflowTimerProcessingPort timers;

    public WorkflowTimerWorker(WorkflowTimerProcessingPort timers) {
        this.timers = timers;
    }

    @Scheduled(fixedDelayString = "${workflow.timer.fixed-delay-ms:1000}")
    public void poll() {
        for (int index = 0; index < MAX_TIMERS_PER_POLL; index++) {
            Optional<WorkflowTimerClaim> claim = timers.claim();
            if (claim.isEmpty()) {
                return;
            }
            WorkflowTimerClaim timer = claim.orElseThrow();
            try {
                timers.process(timer);
            } catch (RuntimeException exception) {
                log.error("Workflow timer transition failed for {}", timer.executionId(), exception);
            }
        }
    }
}
