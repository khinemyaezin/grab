package com.grab.app.account.merchant.internal.query.handler;

import com.grab.app.account.merchant.internal.config.MerchantReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.merchant.application.model.read.GetMerchantQuery;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.GetMerchantUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetMerchantQueryHandler implements QueryHandler<GetMerchantQuery, MerchantAccountResult> {

    private final GetMerchantUseCase getMerchantUseCase;

    public GetMerchantQueryHandler(GetMerchantUseCase getMerchantUseCase) {
        this.getMerchantUseCase = getMerchantUseCase;
    }

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
