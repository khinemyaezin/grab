package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessContextResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.ListAccessContextsRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessAssignmentResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.ListAccessAssignmentsRequestMapper;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.read.AccessContextResult;
import com.identity.application.model.read.ListAccessAssignmentsQuery;
import com.identity.application.model.read.ListAccessContextsQuery;
import com.grab.app.shared.security.SecurityPrincipal;
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

    public List<AccessContextResponse> listContexts(String userId) {
        ListAccessContextsQuery query = mapper.toQuery(userId);
        List<AccessContextResult> results = queryBus.dispatch(query);
        return results.stream().map(mapper::toResponse).toList();
    }

    public List<AccessAssignmentResponse> listAssignments(
            String userId,
            SecurityPrincipal principal
    ) {
        var actorScope = actorScopes.resolve(principal);
        ListAccessAssignmentsQuery query = assignmentMapper.toQuery(
                userId,
                actorScope.key(),
                actorScope.id()
        );
        List<AccessAssignmentResult> results = queryBus.dispatch(query);
        return results.stream().map(assignmentMapper::toResponse).toList();
    }
}
