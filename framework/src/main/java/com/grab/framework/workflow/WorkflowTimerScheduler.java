package com.grab.framework.workflow;

import java.util.List;

public interface WorkflowTimerScheduler {

    void schedule(String executionId, List<WorkflowTimerIntent> timers);
}
