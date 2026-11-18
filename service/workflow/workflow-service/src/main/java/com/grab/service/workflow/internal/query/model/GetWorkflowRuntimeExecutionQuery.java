package com.grab.service.workflow.internal.query.model;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort.WorkflowExecutionSummary;

import java.util.Optional;

public record GetWorkflowRuntimeExecutionQuery(
        Id executionId,
        String callerScope
) implements Query<Optional<WorkflowExecutionSummary>> {
}
