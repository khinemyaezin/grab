package com.grab.framework.workflow;

import java.util.List;

public interface WorkflowDispatchPublisher {

    void publish(String executionId, List<WorkflowDispatch> dispatches);
}
