package com.grab.framework.workflow;

import java.util.List;
import java.util.Optional;

public interface WorkflowQueryStore {

    Optional<WorkflowExecutionMetadata> findExecution(String executionId, String callerScope);

    List<WorkflowNodeView> listNodes(String executionId, String callerScope);

    List<WorkflowHistoryEntry> listHistory(String executionId, String callerScope, int page, int size);

    List<WorkflowIncidentView> listIncidents(String callerScope, int page, int size);
}
