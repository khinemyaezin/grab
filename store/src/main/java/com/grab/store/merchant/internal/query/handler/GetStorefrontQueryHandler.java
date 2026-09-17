package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.command.StorefrontResult;
import com.grab.store.merchant.internal.command.handler.StorefrontOwnership;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.grab.store.merchant.internal.query.GetStorefrontQuery;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.repository.StorefrontRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class GetStorefrontQueryHandler implements QueryHandler<GetStorefrontQuery, StorefrontResult> {
    private final StorefrontRepository storefronts;

    @Override
    @MerchantReadTransactional
    public StorefrontResult handle(GetStorefrontQuery query) {
        Storefront storefront = StorefrontOwnership.requireOwned(
                storefronts, query.storefrontId(), query.merchantId());
        return StorefrontResult.from(storefront);
    }

    @Override
    public Class<GetStorefrontQuery> getQueryType() {
        return GetStorefrontQuery.class;
    }
}
