package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.UserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.GetUserProfileRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.ListUsersRequestMapper;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import com.identity.application.model.read.ListUsersQuery;
import com.identity.application.model.read.ListUsersResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final QueryBus queryBus;
    private final GetUserProfileRequestMapper getUserProfileMapper;
    private final ListUsersRequestMapper listUsersMapper;

    public UserProfileResponse getUser(String id) {
        GetUserProfileQuery query = getUserProfileMapper.toQuery(id);
        GetUserProfileResult result = queryBus.dispatch(query);
        return getUserProfileMapper.toResponse(result);
    }

    public Page<UserProfileResponse> listUsers(Pageable pageable) {
        ListUsersQuery query = listUsersMapper.toQuery(pageable);
        Page<ListUsersResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listUsersMapper::toResponse);
    }
}
