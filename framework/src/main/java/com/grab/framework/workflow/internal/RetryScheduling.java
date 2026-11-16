package com.grab.framework.workflow.internal;

import com.grab.framework.workflow.NodeExecution;
import com.grab.framework.workflow.RetryPolicy;
import com.grab.framework.workflow.TimerPurpose;
import com.grab.framework.workflow.WorkflowFailureClassification;
import com.grab.framework.workflow.WorkflowSignal;
import com.grab.framework.workflow.WorkflowTimerIntent;

import java.time.Duration;
import java.time.Instant;

public final class RetryScheduling {

    private RetryScheduling() {
    }

    public static Decision afterFailure(NodeExecution node, RetryPolicy policy, WorkflowSignal signal,
                                        String fingerprint, Instant processingTime) {
        if (signal.failureClassification() != WorkflowFailureClassification.RETRYABLE
                || node.attempts() >= policy.maxAttempts()) {
            NodeExecution failed = node.fail(signal.error(), fingerprint);
            return new Decision(failed, null);
        }
        Duration delay = policy.delayForAttempt(node.attempts());
        Instant dueAt = processingTime.plus(delay);
        NodeExecution state = node.retryWait(dueAt, signal.error(), fingerprint);
        WorkflowTimerIntent timer = new WorkflowTimerIntent(node.nodeId(), node.logicalOperationId(),
                TimerPurpose.RETRY, state.generation(), dueAt, node.phase());
        return new Decision(state, timer);
    }

    public record Decision(NodeExecution state, WorkflowTimerIntent timer) {
    }
}
