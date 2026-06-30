package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.identity.internal.api.rest.dto.response.AccessContextResponse;
import com.grab.store.identity.internal.api.rest.mapper.ListAccessContextsRequestMapper;
import com.grab.store.identity.internal.api.rest.dto.response.AccessAssignmentResponse;
import com.grab.store.identity.internal.api.rest.mapper.ListAccessAssignmentsRequestMapper;
import com.grab.store.identity.internal.command.AccessAssignmentResult;
import com.grab.store.identity.internal.query.AccessContextResult;
import com.grab.store.identity.internal.query.ListAccessContextsQuery;
import com.grab.store.shared.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessQueryService {
    private final QueryBus queryBus;
    private final ListAccessContextsRequestMapper mapper;
    private final ListAccessAssignmentsRequestMapper assignmentMapper;
    private final AuthenticatedAccessScopeResolver actorScopes;

    public List<AccessContextResponse> listContexts(String userId, String platformCode) {
        ListAccessContextsQuery query = mapper.toQuery(userId, platformCode);
        List<AccessContextResult> results = queryBus.dispatch(query);
        return results.stream().map(mapper::toResponse).toList();
    }

    public List<AccessAssignmentResponse> listAssignments(
            String userId,
            SecurityPrincipal principal
    ) {
        var actorScope = actorScopes.resolve(principal);
        List<AccessAssignmentResult> results = queryBus.dispatch(assignmentMapper.toQuery(
                userId,
                actorScope.key(),
                actorScope.id()
        ));
        return results.stream().map(assignmentMapper::toResponse).toList();
    }
}
