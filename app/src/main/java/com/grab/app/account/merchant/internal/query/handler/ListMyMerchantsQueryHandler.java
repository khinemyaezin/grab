package com.grab.app.account.merchant.internal.query.handler;

import com.grab.app.account.merchant.internal.config.MerchantReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.merchant.application.model.read.ListMyMerchantsQuery;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.ListMyMerchantsUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListMyMerchantsQueryHandler implements QueryHandler<ListMyMerchantsQuery, List<MerchantAccountResult>> {

    private final ListMyMerchantsUseCase listMyMerchantsUseCase;

    public ListMyMerchantsQueryHandler(ListMyMerchantsUseCase listMyMerchantsUseCase) {
        this.listMyMerchantsUseCase = listMyMerchantsUseCase;
    }

    @Override
    @MerchantReadTransactional
    public List<MerchantAccountResult> handle(ListMyMerchantsQuery query) {
        return listMyMerchantsUseCase.execute(query);
    }

    @Override
    public Class<ListMyMerchantsQuery> getQueryType() {
        return ListMyMerchantsQuery.class;
    }
}
