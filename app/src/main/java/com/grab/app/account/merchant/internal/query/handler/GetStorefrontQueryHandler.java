package com.grab.app.account.merchant.internal.query.handler;

import com.grab.app.account.merchant.internal.config.MerchantReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.merchant.application.model.read.GetStorefrontQuery;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.port.inbound.GetStorefrontUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetStorefrontQueryHandler implements QueryHandler<GetStorefrontQuery, StorefrontResult> {

    private final GetStorefrontUseCase getStorefrontUseCase;

    public GetStorefrontQueryHandler(GetStorefrontUseCase getStorefrontUseCase) {
        this.getStorefrontUseCase = getStorefrontUseCase;
    }

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
