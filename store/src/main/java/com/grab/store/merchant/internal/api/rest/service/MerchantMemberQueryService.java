package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.grab.store.merchant.internal.api.rest.mapper.ListMerchantMembersRequestMapper;
import com.grab.store.merchant.internal.config.MerchantEnabled;
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
