package com.grab.service.workflow.internal.adapter.inbound.message;

import com.grab.framework.workflow.WorkflowRuntime;
import com.grab.framework.workflow.WorkflowTimerClaim;
import com.grab.framework.workflow.WorkflowTimerClaimPort;
import com.grab.rpc.adapter.RmiExport;
import com.grab.service.contract.message.workflow.WorkflowTimerProcessingPort;
import com.grab.service.workflow.internal.config.WorkflowConcurrencyRetryable;
import com.grab.service.workflow.internal.config.WorkflowTransactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.rmi.RemoteException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Component
@RmiExport
public class WorkflowTimerProcessingPortAdapter implements WorkflowTimerProcessingPort, com.grab.service.workflow.internal.port.inbound.WorkflowTimerProcessingPort {

    private final WorkflowTimerClaimPort timerClaimPort;
    private final WorkflowRuntime runtime;
    private final Clock clock;
    private final Duration lease;

    public WorkflowTimerProcessingPortAdapter(
            WorkflowTimerClaimPort timerClaimPort,
            WorkflowRuntime runtime,
            @Qualifier("workflowClock") Clock clock,
            @Value("${workflow.timer.lease:PT60S}") String leaseSetting) {
        this.clock = Objects.requireNonNull(clock, "clock");
        Duration parsedLease = Duration.parse(leaseSetting);
        if (parsedLease.isNegative() || parsedLease.isZero()) {
            throw new IllegalArgumentException("Workflow timer lease must be positive");
        }
        this.lease = parsedLease;
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

    @Override
    public void process(WorkflowTimerClaim claim, Instant processingTime) throws RemoteException {
        try {
            process(claim);
        } catch (RuntimeException exception) {
            throw new RemoteException("Workflow timer processing failed: " + exception.getMessage(), exception);
        }
    }
}
