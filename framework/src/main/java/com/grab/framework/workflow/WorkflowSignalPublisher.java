package com.grab.framework.workflow;

import com.grab.framework.domain.Event;

import java.util.List;

public interface WorkflowSignalPublisher {

    void publish(String workflowId, List<Event> events);
}
