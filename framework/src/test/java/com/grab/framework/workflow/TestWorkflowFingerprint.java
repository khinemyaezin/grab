package com.grab.framework.workflow;

import java.util.Arrays;

final class TestWorkflowFingerprint implements WorkflowFingerprint {

    @Override
    public int algorithmVersion() {
        return 2;
    }

    @Override
    public WorkflowFingerprintValue start(String name, int version, int payloadVersion, Object input) {
        String digest = Arrays.deepToString(new Object[]{name, version, payloadVersion, input});
        return new WorkflowFingerprintValue(algorithmVersion(), digest);
    }

    @Override
    public WorkflowFingerprintValue signal(WorkflowSignal signal) {
        String digest = Arrays.deepToString(new Object[]{signal.executionId(), signal.nodeId(), signal.logicalOperationId(),
                signal.attemptId(), signal.attempt(), signal.phase(), signal.outcome(), signal.payload(),
                signal.failureClassification(), signal.errorCode(), signal.error(), signal.payloadVersion()});
        return new WorkflowFingerprintValue(algorithmVersion(), digest);
    }

    @Override
    public WorkflowFingerprintValue transport(WorkflowSignal signal) {
        WorkflowFingerprintValue outcome = signal(signal);
        return new WorkflowFingerprintValue(algorithmVersion(), outcome.digest() + signal.occurredAt());
    }
}
