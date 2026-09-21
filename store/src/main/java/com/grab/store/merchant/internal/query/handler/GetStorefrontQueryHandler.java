package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.merchant.application.port.inbound.GetStorefrontUseCase;
import com.merchant.application.model.read.GetStorefrontQuery;
import com.merchant.application.model.write.StorefrontResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetStorefrontQueryHandler implements QueryHandler<GetStorefrontQuery, StorefrontResult> {

    private final GetStorefrontUseCase getStorefrontUseCase;

    @Override
    @MerchantReadTransactional
    public StorefrontResult handle(GetStorefrontQuery query) {
        return getStorefrontUseCase.execute(query);
    }

    @Override
    public Class<GetStorefrontQuery> getQueryType() {
        return GetStorefrontQuery.class;
    }
}
