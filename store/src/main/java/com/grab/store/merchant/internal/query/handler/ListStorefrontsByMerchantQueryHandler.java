package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.port.inbound.ListStorefrontsByMerchantUseCase;
import com.merchant.application.model.read.ListStorefrontsByMerchantQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListStorefrontsByMerchantQueryHandler implements QueryHandler<ListStorefrontsByMerchantQuery, List<StorefrontResult>> {

    private final ListStorefrontsByMerchantUseCase listStorefrontsByMerchantUseCase;

    @Override
    @MerchantReadTransactional
    public List<StorefrontResult> handle(ListStorefrontsByMerchantQuery query) {
        return listStorefrontsByMerchantUseCase.execute(query);
    }

    @Override
    public Class<ListStorefrontsByMerchantQuery> getQueryType() {
        return ListStorefrontsByMerchantQuery.class;
    }
}
