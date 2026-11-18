package com.grab.service.workflow.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.workflow.WorkflowIncidentView;
import com.grab.framework.workflow.WorkflowQueryStore;
import com.grab.service.contract.message.workflow.WorkflowRuntimeQueryPort;
import com.grab.service.workflow.internal.config.WorkflowReadTransactional;
import com.grab.service.workflow.internal.query.model.ListWorkflowRuntimeIncidentsQuery;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListWorkflowRuntimeIncidentsQueryHandler
        implements QueryHandler<ListWorkflowRuntimeIncidentsQuery, List<WorkflowRuntimeQueryPort.WorkflowIncidentView>> {

    private final WorkflowQueryStore queryStore;

    public ListWorkflowRuntimeIncidentsQueryHandler(WorkflowQueryStore queryStore) {
        this.queryStore = queryStore;
    }

    @Override
    @WorkflowReadTransactional
    public List<WorkflowRuntimeQueryPort.WorkflowIncidentView> handle(ListWorkflowRuntimeIncidentsQuery query) {
        return queryStore.listIncidents(query.callerScope(), query.page(), query.size()).stream()
                .map(this::toView)
                .toList();
    }

    private WorkflowRuntimeQueryPort.WorkflowIncidentView toView(WorkflowIncidentView incident) {
        return new WorkflowRuntimeQueryPort.WorkflowIncidentView(
                new CommonId(incident.executionId()),
                incident.definitionName(),
                incident.nodeId(),
                incident.status(),
                incident.reason(),
                incident.occurredAt()
        );
    }

    @Override
    public Class<ListWorkflowRuntimeIncidentsQuery> getQueryType() {
        return ListWorkflowRuntimeIncidentsQuery.class;
    }
}
