package com.grab.service.account.identity.internal.adapter.inbound.message;

import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.ListAccessAssignmentsPortMapper;
import com.grab.service.account.identity.internal.adapter.inbound.message.mapper.ListAccessContextsPortMapper;
import com.grab.service.contract.message.account.identity.AccessAssignmentPort.AssignmentMessageResult;
import com.grab.service.contract.message.account.identity.AccessAssignmentPort.ContextMessageResult;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.application.port.outbound.SellerScopeLifecyclePort;
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
    private final SellerScopeLifecyclePort lifecycle;
    private final IdGenerator ids;

    public AccessAssignmentReadAdapter(
            @Qualifier("accountQueryBus") QueryBus queryBus,
            ListAccessAssignmentsPortMapper listAssignmentsMapper,
            ListAccessContextsPortMapper listContextsMapper,
            SellerScopeLifecyclePort lifecycle,
            IdGenerator ids
    ) {
        this.queryBus = queryBus;
        this.listAssignmentsMapper = listAssignmentsMapper;
        this.listContextsMapper = listContextsMapper;
        this.lifecycle = lifecycle;
        this.ids = ids;
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
        return results.stream().filter(context -> {
            Id scopeId = ids.convertIdFrom(context.scopeId());
            return lifecycle.find(context.scopeKey(), scopeId).isPresent();
        }).map(listContextsMapper::toResponse).toList();
    }
}
