package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.merchant.application.port.inbound.GetC2CApplicationUseCase;
import com.merchant.application.model.read.GetC2CApplicationQuery;
import com.merchant.application.model.read.GetC2CApplicationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetC2CApplicationQueryHandler implements QueryHandler<GetC2CApplicationQuery, GetC2CApplicationResult> {

    private final GetC2CApplicationUseCase getC2CApplicationUseCase;

    @Override
    @MerchantReadTransactional
    public GetC2CApplicationResult handle(GetC2CApplicationQuery query) {
        return getC2CApplicationUseCase.execute(query);
    }

    @Override
    public Class<GetC2CApplicationQuery> getQueryType() {
        return GetC2CApplicationQuery.class;
    }
}
