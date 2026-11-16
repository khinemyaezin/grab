package com.grab.framework.workflow;

public interface WorkflowFingerprint {

    int algorithmVersion();

    WorkflowFingerprintValue start(String definitionName, int definitionVersion, int payloadVersion, Object input);

    WorkflowFingerprintValue signal(WorkflowSignal signal);

    WorkflowFingerprintValue transport(WorkflowSignal signal);
}
