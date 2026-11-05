package com.grab.app.account.merchant.internal.query.handler;

import com.grab.app.account.merchant.internal.config.MerchantReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.merchant.application.model.read.ListStorefrontsByMerchantQuery;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.port.inbound.ListStorefrontsByMerchantUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListStorefrontsByMerchantQueryHandler
        implements QueryHandler<ListStorefrontsByMerchantQuery, List<StorefrontResult>> {

    private final ListStorefrontsByMerchantUseCase listStorefrontsByMerchantUseCase;

    public ListStorefrontsByMerchantQueryHandler(ListStorefrontsByMerchantUseCase listStorefrontsByMerchantUseCase) {
        this.listStorefrontsByMerchantUseCase = listStorefrontsByMerchantUseCase;
    }

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
