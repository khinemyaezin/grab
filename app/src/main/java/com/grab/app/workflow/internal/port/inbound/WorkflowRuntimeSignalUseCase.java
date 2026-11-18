package com.grab.app.workflow.internal.port.inbound;

import com.grab.app.workflow.port.message.WorkflowRuntimeSignalPort;

public interface WorkflowRuntimeSignalUseCase {

    WorkflowRuntimeSignalPort.SignalResult accept(WorkflowRuntimeSignalPort.SignalMessage message);
}
