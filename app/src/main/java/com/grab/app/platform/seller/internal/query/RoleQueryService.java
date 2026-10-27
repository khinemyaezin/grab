package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.RoleResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.RoleSearchResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.ListRolesRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.SearchRolesRequestMapper;
import com.identity.application.model.read.ListRolesQuery;
import com.identity.application.model.read.ListRolesResult;
import com.identity.application.model.read.SearchRolesQuery;
import com.identity.application.model.read.SearchRolesResult;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleQueryService {

    private final QueryBus queryBus;
    private final ListRolesRequestMapper listRolesMapper;
    private final SearchRolesRequestMapper searchRolesMapper;

    public Page<RoleResponse> listRoles(Pageable pageable) {
        ListRolesQuery query = listRolesMapper.toQuery(pageable);
        Page<ListRolesResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listRolesMapper::toResponse);
    }

    public List<RoleSearchResponse> searchRoles(String name) {
        SearchRolesQuery query = searchRolesMapper.toQuery(name);
        SearchRolesResult result = queryBus.dispatch(query);
        return searchRolesMapper.toResponse(result);
    }
}
