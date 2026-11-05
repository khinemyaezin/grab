package com.grab.app.workflow.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Saga Execution status and timeline queries.
 */
public interface WorkflowQueryPort {

    Optional<WorkflowExecutionPort.WorkflowExecutionResult> getWorkflowStatus(Id workflowId);

    List<WorkflowExecutionPort.WorkflowExecutionResult> listRecentWorkflows(int page, int size);
}

