package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.command.StorefrontResult;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.grab.store.merchant.internal.query.ListStorefrontsByMerchantQuery;
import com.merchant.infrastructure.repository.jpa.StorefrontQueryRepository;
import com.merchant.infrastructure.specification.jpa.StorefrontQueryCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class ListStorefrontsByMerchantQueryHandler
        implements QueryHandler<ListStorefrontsByMerchantQuery, List<StorefrontResult>> {

    private final StorefrontQueryRepository storefronts;

    @Override
    @MerchantReadTransactional
    public List<StorefrontResult> handle(ListStorefrontsByMerchantQuery query) {
        return storefronts.list(new StorefrontQueryCriteria(query.merchantId().getValue()))
                .stream()
                .map(StorefrontResult::from)
                .toList();
    }

    @Override
    public Class<ListStorefrontsByMerchantQuery> getQueryType() {
        return ListStorefrontsByMerchantQuery.class;
    }
}
