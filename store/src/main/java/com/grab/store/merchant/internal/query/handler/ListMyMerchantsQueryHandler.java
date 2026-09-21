package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.ListMyMerchantsUseCase;
import com.merchant.application.model.read.ListMyMerchantsQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListMyMerchantsQueryHandler implements QueryHandler<ListMyMerchantsQuery, List<MerchantAccountResult>> {

    private final ListMyMerchantsUseCase listMyMerchantsUseCase;

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
