package com.grab.app.workflow.internal.query.model;

import com.grab.app.workflow.port.message.WorkflowExecutionPort.WorkflowExecutionResult;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.Optional;

public record GetWorkflowStatusQuery(
        Id workflowId
) implements Query<Optional<WorkflowExecutionResult>> {
}

