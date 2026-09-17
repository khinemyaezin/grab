package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.merchant.internal.api.rest.dto.response.StorefrontResponse;
import com.grab.store.merchant.internal.api.rest.mapper.StorefrontQueryRequestMapper;
import com.grab.store.merchant.internal.command.StorefrontResult;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@MerchantEnabled
@RequiredArgsConstructor
public class StorefrontQueryService {
    private final QueryBus queries;
    private final StorefrontQueryRequestMapper mapper;

    public StorefrontResponse get(String storefrontId, String merchantId) {
        StorefrontResult result = queries.dispatch(mapper.toGetQuery(storefrontId, merchantId));
        return mapper.toResponse(result);
    }

    public List<StorefrontResponse> list(String merchantId) {
        List<StorefrontResult> results = queries.dispatch(mapper.toListQuery(merchantId));
        return mapper.toResponse(results);
    }
}
