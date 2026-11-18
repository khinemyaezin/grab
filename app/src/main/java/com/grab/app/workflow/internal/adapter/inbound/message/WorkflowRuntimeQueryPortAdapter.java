package com.grab.app.workflow.internal.adapter.inbound.message;

import com.grab.app.workflow.internal.adapter.inbound.message.mapper.GetWorkflowRuntimeExecutionPortMapper;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.ListWorkflowRuntimeNodesPortMapper;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.ListWorkflowRuntimeHistoryPortMapper;
import com.grab.app.workflow.internal.adapter.inbound.message.mapper.ListWorkflowRuntimeIncidentsPortMapper;
import com.grab.app.workflow.internal.query.model.GetWorkflowRuntimeExecutionQuery;
import com.grab.app.workflow.internal.query.model.ListWorkflowRuntimeHistoryQuery;
import com.grab.app.workflow.internal.query.model.ListWorkflowRuntimeIncidentsQuery;
import com.grab.app.workflow.internal.query.model.ListWorkflowRuntimeNodesQuery;
import com.grab.app.workflow.port.message.WorkflowRuntimeQueryPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WorkflowRuntimeQueryPortAdapter implements WorkflowRuntimeQueryPort {

    private final QueryBus queryBus;
    private final GetWorkflowRuntimeExecutionPortMapper getExecutionMapper;
    private final ListWorkflowRuntimeNodesPortMapper listNodesMapper;
    private final ListWorkflowRuntimeHistoryPortMapper listHistoryMapper;
    private final ListWorkflowRuntimeIncidentsPortMapper listIncidentsMapper;

    public WorkflowRuntimeQueryPortAdapter(@Qualifier("workflowQueryBus") QueryBus queryBus,
            GetWorkflowRuntimeExecutionPortMapper getExecutionMapper,
            ListWorkflowRuntimeNodesPortMapper listNodesMapper,
            ListWorkflowRuntimeHistoryPortMapper listHistoryMapper,
            ListWorkflowRuntimeIncidentsPortMapper listIncidentsMapper) {
        this.queryBus = queryBus;
        this.getExecutionMapper = getExecutionMapper;
        this.listNodesMapper = listNodesMapper;
        this.listHistoryMapper = listHistoryMapper;
        this.listIncidentsMapper = listIncidentsMapper;
    }

    @Override
    public Optional<WorkflowExecutionSummary> getExecution(Id executionId, String callerScope) {
        GetWorkflowRuntimeExecutionQuery query = getExecutionMapper.toQuery(executionId, callerScope);
        Optional<WorkflowExecutionSummary> result = queryBus.dispatch(query);
        return result.map(getExecutionMapper::toResponse);
    }

    @Override
    public List<WorkflowNodeView> listNodes(Id executionId, String callerScope) {
        ListWorkflowRuntimeNodesQuery query = listNodesMapper.toQuery(executionId, callerScope);
        List<WorkflowNodeView> result = queryBus.dispatch(query);
        return result.stream().map(listNodesMapper::toResponse).toList();
    }

    @Override
    public List<WorkflowHistoryEntry> listHistory(Id executionId, String callerScope, int page, int size) {
        ListWorkflowRuntimeHistoryQuery query = listHistoryMapper.toQuery(executionId, callerScope, page, size);
        List<WorkflowHistoryEntry> result = queryBus.dispatch(query);
        return result.stream().map(listHistoryMapper::toResponse).toList();
    }

    @Override
    public List<WorkflowIncidentView> listIncidents(String callerScope, int page, int size) {
        ListWorkflowRuntimeIncidentsQuery query = listIncidentsMapper.toQuery(callerScope, page, size);
        List<WorkflowIncidentView> result = queryBus.dispatch(query);
        return result.stream().map(listIncidentsMapper::toResponse).toList();
    }
}
