package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.merchant.application.port.inbound.GetMerchantUseCase;
import com.merchant.application.model.read.GetMerchantQuery;
import com.merchant.application.model.write.MerchantAccountResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetMerchantQueryHandler implements QueryHandler<GetMerchantQuery, MerchantAccountResult> {

    private final GetMerchantUseCase getMerchantUseCase;

    @Override
    @MerchantReadTransactional
    public MerchantAccountResult handle(GetMerchantQuery query) {
        return getMerchantUseCase.execute(query);
    }

    @Override
    public Class<GetMerchantQuery> getQueryType() {
        return GetMerchantQuery.class;
    }
}
