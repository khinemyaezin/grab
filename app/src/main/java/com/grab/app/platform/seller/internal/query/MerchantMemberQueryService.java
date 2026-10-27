package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.ListMerchantMembersRequestMapper;
import com.grab.app.account.merchant.internal.config.MerchantEnabled;
import com.merchant.application.model.read.ListMerchantMembersQuery;
import com.merchant.application.model.write.MerchantMemberResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantMemberQueryService {
    private final QueryBus queries;
    private final ListMerchantMembersRequestMapper listMapper;

    public List<MerchantMemberResponse> list(String merchantId) {
        ListMerchantMembersQuery query = listMapper.toQuery(merchantId);
        List<MerchantMemberResult> results = queries.dispatch(query);
        return listMapper.toResponse(results);
    }
}
