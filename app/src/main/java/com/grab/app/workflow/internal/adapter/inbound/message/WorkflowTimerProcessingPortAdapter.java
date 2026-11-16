package com.grab.app.workflow.internal.adapter.inbound.message;

import com.grab.app.workflow.internal.config.WorkflowTransactional;
import com.grab.app.workflow.internal.port.inbound.WorkflowTimerProcessingPort;
import com.grab.framework.workflow.WorkflowRuntime;
import com.grab.framework.workflow.WorkflowTimerClaim;
import com.grab.framework.workflow.WorkflowTimerClaimPort;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import com.grab.app.workflow.internal.config.WorkflowConcurrencyRetryable;

import java.time.Instant;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.Objects;

@Component
public class WorkflowTimerProcessingPortAdapter implements WorkflowTimerProcessingPort {

    private final WorkflowTimerClaimPort timerClaimPort;
    private final WorkflowRuntime runtime;

    private final Clock clock;
    private final Duration lease;

    public WorkflowTimerProcessingPortAdapter(WorkflowTimerClaimPort timerClaimPort, WorkflowRuntime runtime,
                                               @Qualifier("workflowClock") Clock clock,
                                               @Value("${workflow.timer.lease:PT60S}") String leaseSetting) {
        this.clock = Objects.requireNonNull(clock, "clock");
        Duration lease = Duration.parse(leaseSetting);
        if (lease.isNegative() || lease.isZero()) {
            throw new IllegalArgumentException("Workflow timer lease must be positive");
        }
        this.lease = lease;
        this.timerClaimPort = Objects.requireNonNull(timerClaimPort, "timerClaimPort");
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @Override
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<WorkflowTimerClaim> claim() {
        Instant now = clock.instant();
        return timerClaimPort.claimDue(now, 1, lease).stream().findFirst();
    }

    @Override
    @WorkflowConcurrencyRetryable
    @WorkflowTransactional(propagation = Propagation.REQUIRES_NEW)
    public void process(WorkflowTimerClaim claim) {
        Objects.requireNonNull(claim, "claim");
        if (!timerClaimPort.isCurrent(claim, clock)) {
            return;
        }
        Instant processingTime = clock.instant();
        if (!processingTime.isBefore(claim.leaseExpiresAt())) {
            return;
        }
        runtime.onTimer(claim, processingTime);
        timerClaimPort.markFired(claim);
    }
}
