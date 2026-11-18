package com.grab.framework.workflow;

public interface WorkflowHistoryStore {

    long nextSequence(String executionId);

    void append(WorkflowHistoryEntry entry);
}
