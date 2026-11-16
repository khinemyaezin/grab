package com.grab.framework.workflow;

import java.time.Duration;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

public interface WorkflowTimerClaimPort {

    List<WorkflowTimerClaim> claimDue(Instant now, int limit, Duration lease);

    /** Returns true only while this worker still owns the leased timer claim. */
    boolean isCurrent(WorkflowTimerClaim claim, Clock clock);

    void markFired(WorkflowTimerClaim claim);
}
