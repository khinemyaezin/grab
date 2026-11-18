package com.grab.service.workflow.internal.query.model;

import com.grab.framework.cqrs.query.Query;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort.WorkflowIncidentView;

import java.util.List;

public record ListWorkflowRuntimeIncidentsQuery(
        String callerScope,
        int page,
        int size
) implements Query<List<WorkflowIncidentView>> {
}
