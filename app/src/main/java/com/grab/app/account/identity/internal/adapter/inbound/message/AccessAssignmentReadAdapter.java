package com.grab.app.account.identity.internal.adapter.inbound.message;

import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ListAccessAssignmentsPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ListAccessContextsPortMapper;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.AssignmentMessageResult;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.ContextMessageResult;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.identity.application.model.read.AccessContextResult;
import com.identity.application.model.read.ListAccessAssignmentsQuery;
import com.identity.application.model.read.ListAccessContextsQuery;
import com.identity.application.model.write.AccessAssignmentResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AccessAssignmentReadAdapter {

    private final QueryBus queryBus;
    private final ListAccessAssignmentsPortMapper listAssignmentsMapper;
    private final ListAccessContextsPortMapper listContextsMapper;

    public AccessAssignmentReadAdapter(
            @Qualifier("accountQueryBus") QueryBus queryBus,
            ListAccessAssignmentsPortMapper listAssignmentsMapper,
            ListAccessContextsPortMapper listContextsMapper
    ) {
        this.queryBus = queryBus;
        this.listAssignmentsMapper = listAssignmentsMapper;
        this.listContextsMapper = listContextsMapper;
    }

    public List<AssignmentMessageResult> listAssignmentsForUser(Id userId, String actorScopeKey, String actorScopeId) {
        ListAccessAssignmentsQuery query = listAssignmentsMapper.toQuery(userId, actorScopeKey, actorScopeId);
        List<AccessAssignmentResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(listAssignmentsMapper::toResponse).toList();
    }

    public List<ContextMessageResult> listAvailableContexts(Id userId) {
        ListAccessContextsQuery query = listContextsMapper.toQuery(userId);
        List<AccessContextResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(listContextsMapper::toResponse).toList();
    }
}
