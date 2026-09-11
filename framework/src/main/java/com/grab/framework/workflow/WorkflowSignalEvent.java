package com.grab.framework.workflow;

import com.grab.framework.domain.Event;

public interface WorkflowSignalEvent extends Event {

    SignalType signalType();

    String signalDedupKey();

    default String signalWorkflowId() {
        return null;
    }

    default CorrelationKey signalCorrelation() {
        return null;
    }
}
