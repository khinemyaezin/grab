package com.grab.app.account.merchant.internal.query.handler;

import com.grab.app.account.merchant.internal.config.MerchantReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.merchant.application.model.read.GetC2CApplicationQuery;
import com.merchant.application.model.read.GetC2CApplicationResult;
import com.merchant.application.port.inbound.GetC2CApplicationUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetC2CApplicationQueryHandler implements QueryHandler<GetC2CApplicationQuery, GetC2CApplicationResult> {

    private final GetC2CApplicationUseCase getC2CApplicationUseCase;

    public GetC2CApplicationQueryHandler(GetC2CApplicationUseCase getC2CApplicationUseCase) {
        this.getC2CApplicationUseCase = getC2CApplicationUseCase;
    }

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
